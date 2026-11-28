package com.work.assistant.biz.monitor.model;

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
 * 定时任务执行记录。
 *
 * <p>记录每个 {@code @Scheduled} 定时任务每次执行的结果，用于系统监控面板的
 * 「定时任务监控」区块展示执行历史与失败告警。
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "task_run_log")
public class TaskRunLog {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 任务标识（如 hrmsCardRecordPull）
   */
  @Column(name = "task_name")
  private String taskName;

  /**
   * 开始时间
   */
  @Column(name = "start_time")
  private LocalDateTime startTime;

  /**
   * 结束时间
   */
  @Column(name = "end_time")
  private LocalDateTime endTime;

  /**
   * 执行耗时（毫秒）
   */
  @Column(name = "duration_ms")
  private Long durationMs;

  /**
   * 执行结果：SUCCESS / FAILED
   */
  @Column(name = "status")
  private String status;

  /**
   * 失败时的异常信息
   */
  @Column(name = "error_msg")
  private String errorMsg;

  /**
   * 记录时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
