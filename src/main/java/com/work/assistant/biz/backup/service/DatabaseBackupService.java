package com.work.assistant.biz.backup.service;

import com.work.assistant.biz.backup.model.DatabaseBackup;
import com.work.assistant.biz.backup.repository.DatabaseBackupRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据库逻辑备份与导入服务。
 *
 * <p>采用纯 JDBC 逻辑备份（生成标准 SQL 脚本）而非依赖外部 {@code pg_dump} 工具，
 * 保证在未安装 PostgreSQL 客户端的环境下（数据库位于远程服务器）也能正常备份/导入。
 *
 * <p>备份产物为一份可读的 {@code .sql} 文件，仅包含各表的 {@code INSERT} 数据语句
 * （不包含表结构），存储于 {@code work.assistant.backup.dir} 目录。
 *
 * @author work-assistant
 */
@Slf4j
@Service
public class DatabaseBackupService {

  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  /**
   * 备份状态：成功。
   */
  public static final String STATUS_SUCCESS = "SUCCESS";

  /**
   * 备份状态：失败。
   */
  public static final String STATUS_FAILED = "FAILED";

  /**
   * 备份类型：全量备份。
   */
  public static final String TYPE_FULL = "FULL";

  /**
   * 备份类型：部分备份（仅指定表）。
   */
  public static final String TYPE_PARTIAL = "PARTIAL";

  private final DataSource dataSource;
  private final DatabaseBackupRepository backupRepository;

  @Value("${work.assistant.backup.dir:./data/backups}")
  private String backupDir;

  public DatabaseBackupService(DataSource dataSource,
                               DatabaseBackupRepository backupRepository) {
    this.dataSource = dataSource;
    this.backupRepository = backupRepository;
  }

  /**
   * 查询全部备份记录（按时间倒序）。
   */
  public List<DatabaseBackup> list() {
    return backupRepository.findAllByOrderByCreatedAtDesc();
  }

  /**
   * 按主键查询单条备份记录，不存在时返回 {@code null}。
   */
  public DatabaseBackup findById(String id) {
    return backupRepository.findById(id).orElse(null);
  }

  /**
   * 执行一次逻辑备份（全量），返回备份记录。
   */
  public DatabaseBackup backup() {
    return backup(null);
  }

  /**
   * 执行一次逻辑备份，返回备份记录。
   *
   * @param selectedTables 要备份的表名清单；为 {@code null} 或空表示全量备份
   */
  public DatabaseBackup backup(List<String> selectedTables) {
    boolean partial = selectedTables != null && !selectedTables.isEmpty();
    DatabaseBackup record = new DatabaseBackup();
    record.setCreatedAt(LocalDateTime.now());
    record.setStatus(STATUS_SUCCESS);
    record.setBackupType(partial ? TYPE_PARTIAL : TYPE_FULL);

    String prefix = partial ? "backup_partial_" : "backup_full_";
    String fileName = prefix + LocalDateTime.now().format(FILE_TS) + ".sql";
    Path dir = resolveBackupDir();
    Path file = dir.resolve(fileName);

    int tableCount = 0;
    try {
      Files.createDirectories(dir);
      StringBuilder sb = new StringBuilder();
      sb.append("-- 数据库数据备份（仅数据，不含表结构）\n")
        .append("-- 类型: ").append(partial ? "部分备份" : "全量备份").append("\n")
        .append("-- 生成时间: ").append(LocalDateTime.now()).append("\n\n");

      try (Connection conn = dataSource.getConnection()) {
        // 部分备份时校验并规范化表名（防止 SQL 注入/非法表名）
        List<String> tables;
        if (partial) {
          Set<String> allTables = new HashSet<>(listTables(conn));
          tables = new ArrayList<>();
          for (String t : selectedTables) {
            if (allTables.contains(t)) {
              tables.add(t);
            }
          }
          if (tables.isEmpty()) {
            throw new IllegalArgumentException("所选表均不存在或无效");
          }
        } else {
          tables = listTables(conn);
        }

        for (String table : tables) {
          appendInsertData(sb, conn, table);
          tableCount++;
        }

        record.setTableList(partial ? String.join(",", tables) : null);
      }

      Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
      record.setFileName(fileName);
      record.setFileSize(Files.size(file));
      record.setTableCount(tableCount);
      log.info("database backup completed: {} ({} tables, {} bytes)",
          fileName, tableCount, record.getFileSize());
    } catch (Exception e) {
      record.setStatus(STATUS_FAILED);
      record.setErrorMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
      log.error("database backup failed", e);
    }

    return backupRepository.save(record);
  }

  /**
   * 列出当前数据库 schema 下的全部用户表（供前端选择部分备份）。
   */
  public List<String> listTables() {
    try (Connection conn = dataSource.getConnection()) {
      return listTables(conn);
    } catch (SQLException e) {
      log.error("list tables error", e);
      return List.of();
    }
  }

  /**
   * 导入备份文件：执行 SQL 脚本中的全部语句。
   *
   * <p>备份仅含 {@code INSERT} 数据语句，导入时直接向现有表写入数据，
   * 不重建表结构。
   */
  public int importSql(MultipartFile file) throws IOException {
    String sql = new String(file.getBytes(), StandardCharsets.UTF_8);
    return executeSqlScript(sql);
  }

  /**
   * 下载备份文件内容。
   */
  public Path resolveFile(String fileName) {
    Path file = resolveBackupDir().resolve(fileName).normalize();
    if (!file.startsWith(resolveBackupDir().normalize())) {
      throw new IllegalArgumentException("非法文件名");
    }
    if (!Files.exists(file)) {
      throw new IllegalArgumentException("备份文件不存在");
    }
    return file;
  }

  /**
   * 删除备份记录及对应文件。
   */
  public void delete(String id) {
    DatabaseBackup record = findById(id);
    if (record == null) {
      return;
    }
    backupRepository.delete(record);
    if (record.getFileName() != null) {
      try {
        Files.deleteIfExists(resolveBackupDir().resolve(record.getFileName()));
      } catch (IOException e) {
        log.warn("delete backup file failed: {}", record.getFileName(), e);
      }
    }
  }

  /* ==================== 内部实现 ==================== */

  private Path resolveBackupDir() {
    return Path.of(backupDir).toAbsolutePath().normalize();
  }

  /**
   * 列出当前数据库 schema 下的全部用户表。
   */
  private List<String> listTables(Connection conn) throws SQLException {
    List<String> tables = new ArrayList<>();
    DatabaseMetaData meta = conn.getMetaData();
    String schema = conn.getSchema();
    if (schema == null || schema.isBlank()) {
      schema = "public";
    }
    try (ResultSet rs = meta.getTables(null, schema, "%", new String[] {"TABLE"})) {
      while (rs.next()) {
        String name = rs.getString("TABLE_NAME");
        tables.add(name);
      }
    }
    return tables;
  }

  /**
   * 生成某表的数据 {@code INSERT} 语句。
   */
  private void appendInsertData(StringBuilder sb, Connection conn, String table) throws SQLException {
    String schema = conn.getSchema();
    if (schema == null || schema.isBlank()) {
      schema = "public";
    }
    String fullName = quoteIdent(schema) + "." + quoteIdent(table);

    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM " + fullName)) {
      ResultSetMetaData rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();

      String[] colNames = new String[colCount];
      for (int i = 0; i < colCount; i++) {
        colNames[i] = rsmd.getColumnName(i + 1);
      }

      StringBuilder insertHeader = new StringBuilder();
      insertHeader.append("INSERT INTO ").append(fullName).append(" (");
      for (int i = 0; i < colCount; i++) {
        if (i > 0) insertHeader.append(", ");
        insertHeader.append(quoteIdent(colNames[i]));
      }
      insertHeader.append(") VALUES ");

      boolean any = false;
      while (rs.next()) {
        if (!any) {
          sb.append(insertHeader);
          any = true;
        } else {
          sb.append(",\n       ");
        }
        sb.append("(");
        for (int i = 0; i < colCount; i++) {
          if (i > 0) sb.append(", ");
          sb.append(formatValue(rs, i + 1));
        }
        sb.append(")");
      }
      if (any) {
        sb.append(";\n\n");
      }
    }
  }

  /**
   * 将结果集中的值格式化为 SQL 字面量（正确处理 NULL、字符串转义、时间戳等）。
   */
  private String formatValue(ResultSet rs, int index) throws SQLException {
    String val = rs.getString(index);
    if (val == null) {
      return "NULL";
    }
    // 字符串类型需转义单引号；其余类型（数字/布尔）直接输出
    int sqlType = rs.getMetaData().getColumnType(index);
    if (isNumeric(sqlType) || isBoolean(sqlType)) {
      return val;
    }
    return "'" + val.replace("'", "''") + "'";
  }

  private boolean isNumeric(int sqlType) {
    return sqlType == java.sql.Types.TINYINT
        || sqlType == java.sql.Types.SMALLINT
        || sqlType == java.sql.Types.INTEGER
        || sqlType == java.sql.Types.BIGINT
        || sqlType == java.sql.Types.FLOAT
        || sqlType == java.sql.Types.REAL
        || sqlType == java.sql.Types.DOUBLE
        || sqlType == java.sql.Types.NUMERIC
        || sqlType == java.sql.Types.DECIMAL;
  }

  private boolean isBoolean(int sqlType) {
    return sqlType == java.sql.Types.BOOLEAN || sqlType == java.sql.Types.BIT;
  }

  private String quoteIdent(String ident) {
    return "\"" + ident.replace("\"", "\"\"") + "\"";
  }

  /**
   * 执行整段 SQL 脚本（按分号切分语句逐条执行）。
   */
  private int executeSqlScript(String sql) {
    List<String> statements = splitStatements(sql);
    int executed = 0;
    try (Connection conn = dataSource.getConnection();
         Statement st = conn.createStatement()) {
      for (String s : statements) {
        String trimmed = s.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("--")) {
          continue;
        }
        st.execute(trimmed);
        executed++;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("导入失败: " + e.getMessage(), e);
    }
    return executed;
  }

  /**
   * 简单按分号切分 SQL 语句（对标准逻辑备份脚本足够；不支持函数体内的分号）。
   */
  private List<String> splitStatements(String sql) {
    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingle = false;
    boolean inDouble = false;
    for (int i = 0; i < sql.length(); i++) {
      char c = sql.charAt(i);
      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
      } else if (c == '"' && !inSingle) {
        inDouble = !inDouble;
      }
      if (c == ';' && !inSingle && !inDouble) {
        result.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    if (current.length() > 0) {
      result.add(current.toString());
    }
    return result;
  }
}
