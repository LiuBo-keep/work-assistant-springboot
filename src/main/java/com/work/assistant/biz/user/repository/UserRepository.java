package com.work.assistant.biz.user.repository;

import com.work.assistant.biz.user.model.SysUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 系统用户仓储。
 *
 * @author work-assistant
 */
@Repository
public interface UserRepository extends JpaRepository<SysUser, String> {

  /**
   * 按用户名查询用户（登录校验用）。
   */
  Optional<SysUser> findByUserName(String userName);

  /**
   * 判断用户名是否已存在（唯一性校验）。
   */
  boolean existsByUserName(String userName);

  /**
   * 查询全部用户（按创建时间倒序）。
   */
  List<SysUser> findAllByOrderByCreatedAtDesc();
}
