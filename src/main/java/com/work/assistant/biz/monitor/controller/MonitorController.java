package com.work.assistant.biz.monitor.controller;

import com.work.assistant.biz.monitor.service.MonitorService;
import com.work.assistant.common.response.RestResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控接口。
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {

  private final MonitorService monitorService;

  public MonitorController(MonitorService monitorService) {
    this.monitorService = monitorService;
  }

  /**
   * 系统概览指标（CPU / 内存 / JVM / GC / 线程 / 类加载）。
   */
  @GetMapping("/overview")
  public RestResponse overview() {
    return RestResponse.success(monitorService.overview());
  }

  /**
   * 指定指标的历史快照。
   *
   * @param metricName 指标名，如 heap.used
   * @param hours      回溯小时数，默认 24
   */
  @GetMapping("/metric-history")
  public RestResponse metricHistory(
      @RequestParam("metricName") String metricName,
      @RequestParam(value = "hours", defaultValue = "24") int hours) {
    return RestResponse.success(monitorService.metricHistory(metricName, hours));
  }

  /**
   * 定时任务执行记录（最近 50 条）。
   */
  @GetMapping("/tasks")
  public RestResponse taskRunLogs() {
    return RestResponse.success(monitorService.taskRunLogs());
  }

  /**
   * 线程监控明细（状态分布 + 存活线程列表 + 死锁检测）。
   */
  @GetMapping("/threads")
  public RestResponse threads() {
    return RestResponse.success(monitorService.threads());
  }

  /**
   * 数据库连接池监控（连接池指标 + 连通性探测）。
   */
  @GetMapping("/database")
  public RestResponse database() {
    return RestResponse.success(monitorService.database());
  }
}
