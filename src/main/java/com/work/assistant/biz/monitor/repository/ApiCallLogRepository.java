package com.work.assistant.biz.monitor.repository;

import com.work.assistant.biz.monitor.model.ApiCallLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 接口调用日志仓库。
 *
 * @author work-assistant
 */
@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, String> {

  /**
   * 按时间倒序查询最近 N 条调用记录。
   */
  List<ApiCallLog> findTop100ByOrderByCreatedAtDesc();

  /**
   * 统计指定时间范围内的调用总次数。
   */
  long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

  /**
   * 统计指定时间范围内、指定状态码范围的调用次数（用于成功率/失败率）。
   */
  long countByCreatedAtBetweenAndStatusCodeBetween(
      LocalDateTime from, LocalDateTime to, int minStatus, int maxStatus);

  /**
   * 按请求路径分组统计调用次数（近 N 小时），用于慢接口/热点接口 TOP。
   */
  @Query("""
      select a.requestPath, count(a), avg(a.durationMs), max(a.durationMs)
      from ApiCallLog a
      where a.createdAt >= :from
      group by a.requestPath
      order by count(a) desc
      """)
  List<Object[]> aggregateByPath(@Param("from") LocalDateTime from);

  /**
   * 按状态码分组统计（近 N 小时），用于状态码分布。
   */
  @Query("""
      select a.statusCode, count(a)
      from ApiCallLog a
      where a.createdAt >= :from
      group by a.statusCode
      order by count(a) desc
      """)
  List<Object[]> aggregateByStatus(@Param("from") LocalDateTime from);
}
