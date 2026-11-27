package com.work.assistant.scheduled.monitor;

import com.work.assistant.biz.monitor.service.MonitorService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时任务执行记录器。
 *
 * <p>对需要被监控面板观测的定时任务，在其执行逻辑前后调用
 * {@link #execute(String, Runnable)}，自动记录开始/结束时间、耗时与结果。
 *
 * <p>采用显式调用而非 AOP 切面，避免引入额外的 aspectj 依赖，改动点清晰可控。
 *
 * @author work-assistant
 */
@Slf4j
@Component
public class TaskExecutionRecorder {

  private final MonitorService monitorService;

  public TaskExecutionRecorder(MonitorService monitorService) {
    this.monitorService = monitorService;
  }

  /**
   * 执行任务并记录执行日志。
   *
   * @param taskName 任务标识
   * @param action   任务实际执行的逻辑
   */
  public void execute(String taskName, Runnable action) {
    LocalDateTime start = LocalDateTime.now();
    boolean success = true;
    String errorMsg = null;
    try {
      action.run();
    } catch (Exception e) {
      success = false;
      errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
      log.error("scheduled task [{}] execute error", taskName, e);
      throw e;
    } finally {
      LocalDateTime end = LocalDateTime.now();
      long durationMs = java.time.Duration.between(start, end).toMillis();
      monitorService.recordTaskRun(taskName, start, end, durationMs, success, errorMsg);
    }
  }
}
