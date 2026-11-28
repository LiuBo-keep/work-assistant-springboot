package com.work.assistant.biz.user.metadata;

import lombok.Data;

/**
 * 用户保存请求（新增 / 修改）。
 *
 * <p>新增时 {@code userName} / {@code password} / {@code role} 必填；
 * 修改时仅 {@code id} 必填，其余字段按需传（密码为空表示不修改密码）。
 *
 * @author work-assistant
 */
@Data
public class UserSaveRequest {

  /**
   * 主键（新增时为空）。
   */
  private String id;

  /**
   * 登录用户名。
   */
  private String userName;

  /**
   * 登录密码（明文，服务端 AES 加密落库）。
   */
  private String password;

  /**
   * 显示名称 / 昵称。
   */
  private String displayName;

  /**
   * 角色：NORMAL / ADMIN。
   */
  private String role;

  /**
   * 是否启用。
   */
  private Boolean enabled;
}
