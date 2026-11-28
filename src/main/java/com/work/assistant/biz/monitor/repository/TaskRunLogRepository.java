package com.work.assistant.biz.monitor.repository;

import com.work.assistant.biz.monitor.model.TaskRunLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 定时任务执行记录仓库。
 *
 * @author work-assistant
 */
@Repository
public interface TaskRunLogRepository extends JpaRepository<TaskRunLog, String> {

  /**
   * 按时间倒序查询最近 N 条记录。
   */
  List<TaskRunLog> findTop50ByOrderByStartTimeDesc();
}
