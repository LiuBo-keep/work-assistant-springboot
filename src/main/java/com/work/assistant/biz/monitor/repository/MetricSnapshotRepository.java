package com.work.assistant.biz.monitor.repository;

import com.work.assistant.biz.monitor.model.MetricSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 系统指标快照仓库。
 *
 * @author work-assistant
 */
@Repository
public interface MetricSnapshotRepository extends JpaRepository<MetricSnapshot, String> {

  /**
   * 按指标名与时间范围查询快照（升序，用于绘制趋势曲线）。
   */
  List<MetricSnapshot> findByMetricNameAndCapturedAtBetweenOrderByCapturedAtAsc(
      String metricName, LocalDateTime from, LocalDateTime to);
}
