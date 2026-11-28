package com.work.assistant.biz.password.repository;

import com.work.assistant.biz.password.model.PasswordEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 密码本仓储。
 *
 * @author work-assistant
 */
@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, String> {

  /**
   * 按所属用户查询全部条目。
   *
   * @param owner 用户名
   * @return 条目列表
   */
  List<PasswordEntry> findByOwnerOrderByUpdatedAtDesc(String owner);
}
