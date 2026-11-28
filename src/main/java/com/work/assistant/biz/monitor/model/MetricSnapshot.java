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
 * 系统指标快照。
 *
 * <p>定时采样 JVM / 内存 / CPU 等关键指标并落库，用于系统监控面板绘制历史趋势曲线。
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "metric_snapshot")
public class MetricSnapshot {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 指标名（如 heap.used、heap.max、nonheap.used）
   */
  @Column(name = "metric_name")
  private String metricName;

  /**
   * 指标值（单位：字节）
   */
  @Column(name = "metric_value")
  private Double metricValue;

  /**
   * 采样时间
   */
  @Column(name = "captured_at")
  private LocalDateTime capturedAt;
}
