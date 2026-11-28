package com.work.assistant.biz.backup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据库备份记录。
 *
 * <p>记录每次逻辑备份产生的 SQL 文件元信息，供「数据库备份」子页面展示与下载。
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "database_backup")
public class DatabaseBackup {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 备份文件名（如 backup_20261128_115000.sql）
   */
  @Column(name = "file_name")
  private String fileName;

  /**
   * 文件大小（字节）
   */
  @Column(name = "file_size")
  private Long fileSize;

  /**
   * 备份包含的表数量
   */
  @Column(name = "table_count")
  private Integer tableCount;

  /**
   * 备份类型：FULL（全量）/ PARTIAL（部分表）
   */
  @Column(name = "backup_type")
  private String backupType;

  /**
   * 备份的表名清单（逗号分隔，部分备份时记录；全量备份为空）
   */
  @Column(name = "table_list")
  private String tableList;

  /**
   * 备份状态：SUCCESS / FAILED
   */
  @Column(name = "status")
  private String status;

  /**
   * 失败时的错误信息
   */
  @Column(name = "error_msg")
  private String errorMsg;

  /**
   * 备份时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
