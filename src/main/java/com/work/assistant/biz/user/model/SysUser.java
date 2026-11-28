package com.work.assistant.biz.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统用户。
 *
 * <p>登录校验与权限控制的数据基础：用户名唯一，密码经服务端 AES 落库加密
 * （{@link com.work.assistant.config.database.AesEncryptorConverter}），
 * 角色（{@link UserRole}）决定前端可见目录范围。
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUser {

  /**
   * 主键。
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 登录用户名（唯一）。
   */
  @Column(name = "user_name", nullable = false, unique = true, length = 64)
  private String userName;

  /**
   * 登录密码（AES 落库加密）。
   */
  @Column(name = "password", nullable = false)
  private String password;

  /**
   * 显示名称 / 昵称。
   */
  @Column(name = "display_name", length = 64)
  private String displayName;

  /**
   * 用户角色：NORMAL（普通用户）/ ADMIN（超管）。
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 16)
  private UserRole role;

  /**
   * 是否启用（禁用后无法登录）。
   */
  @Column(name = "enabled", nullable = false)
  private Boolean enabled = true;

  /**
   * 创建时间。
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * 更新时间。
   */
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
