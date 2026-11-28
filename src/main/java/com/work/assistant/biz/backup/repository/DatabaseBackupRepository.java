package com.work.assistant.biz.backup.repository;

import com.work.assistant.biz.backup.model.DatabaseBackup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据库备份记录仓库。
 *
 * @author work-assistant
 */
@Repository
public interface DatabaseBackupRepository extends JpaRepository<DatabaseBackup, String> {

  /**
   * 按时间倒序查询全部备份记录。
   */
  List<DatabaseBackup> findAllByOrderByCreatedAtDesc();
}
