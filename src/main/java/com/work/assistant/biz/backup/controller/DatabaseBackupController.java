package com.work.assistant.biz.backup.controller;

import com.work.assistant.biz.backup.model.DatabaseBackup;
import com.work.assistant.biz.backup.service.DatabaseBackupService;
import com.work.assistant.common.response.RestResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据库备份控制器。
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/backup")
public class DatabaseBackupController {

  private final DatabaseBackupService backupService;

  public DatabaseBackupController(DatabaseBackupService backupService) {
    this.backupService = backupService;
  }

  /**
   * 备份记录列表（时间倒序）。
   */
  @GetMapping("/list")
  public RestResponse list() {
    List<DatabaseBackup> list = backupService.list();
    return RestResponse.success(list);
  }

  /**
   * 立即执行一次逻辑备份。
   *
   * <p>不传 {@code tables} 参数（或传空）时执行全量备份；
   * 传入表名清单时仅备份所选表的数据（部分备份）。
   */
  @PostMapping("/backup")
  public RestResponse backup(@RequestParam(value = "tables", required = false) List<String> tables) {
    DatabaseBackup record = backupService.backup(tables);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("record", record);
    data.put("success", DatabaseBackupService.STATUS_SUCCESS.equals(record.getStatus()));
    return RestResponse.success(data);
  }

  /**
   * 列出可备份的表名清单（供「部分备份」选择）。
   */
  @GetMapping("/tables")
  public RestResponse tables() {
    List<String> tables = backupService.listTables();
    return RestResponse.success(tables);
  }

  /**
   * 导入备份文件（上传 .sql 脚本并执行）。
   */
  @PostMapping("/import")
  public RestResponse importSql(@RequestParam("file") MultipartFile file) {
    try {
      int executed = backupService.importSql(file);
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("executedStatements", executed);
      data.put("fileName", file.getOriginalFilename());
      return RestResponse.success(data);
    } catch (IOException e) {
      return RestResponse.fail("BACKUP_IMPORT_ERROR", "导入失败: " + e.getMessage());
    }
  }

  /**
   * 下载备份文件。
   */
  @GetMapping("/download/{id}")
  public ResponseEntity<ByteArrayResource> download(@PathVariable("id") String id) {
    DatabaseBackup record = backupService.findById(id);
    if (record == null || record.getFileName() == null) {
      return ResponseEntity.notFound().build();
    }
    Path file = backupService.resolveFile(record.getFileName());
    try {
      byte[] bytes = Files.readAllBytes(file);
      ByteArrayResource resource = new ByteArrayResource(bytes);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION,
              ContentDisposition.attachment()
                  .filename(record.getFileName(), java.nio.charset.StandardCharsets.UTF_8)
                  .build().toString())
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(bytes.length)
          .body(resource);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 删除备份记录及文件。
   */
  @DeleteMapping("/{id}")
  public RestResponse delete(@PathVariable("id") String id) {
    backupService.delete(id);
    return RestResponse.success();
  }
}
