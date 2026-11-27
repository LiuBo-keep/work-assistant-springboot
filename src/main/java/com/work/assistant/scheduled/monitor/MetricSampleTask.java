package com.work.assistant.scheduled.monitor;

import com.work.assistant.biz.monitor.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统指标采样定时任务。
 *
 * <p>每 5 分钟采样一次 JVM 内存指标并落库，用于系统监控面板绘制历史趋势曲线。
 *
 * @author work-assistant
 */
@Slf4j
@Component
public class MetricSampleTask {

  private final MonitorService monitorService;

  public MetricSampleTask(MonitorService monitorService) {
    this.monitorService = monitorService;
  }

  @Scheduled(cron = "0 */5 * * * *")
  public void sample() {
    try {
      monitorService.sampleMetrics();
    } catch (Exception e) {
      log.error("metric sample task error", e);
    }
  }
}
