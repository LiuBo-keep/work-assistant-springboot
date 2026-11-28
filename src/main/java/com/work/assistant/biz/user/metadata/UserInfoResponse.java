package com.work.assistant.biz.user.metadata;

import lombok.Data;

/**
 * 用户信息响应（不含密码）。
 *
 * @author work-assistant
 */
@Data
public class UserInfoResponse {

  /**
   * 主键。
   */
  private String id;

  /**
   * 登录用户名。
   */
  private String userName;

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

  /**
   * 创建时间。
   */
  private String createdAt;

  /**
   * 更新时间。
   */
  private String updatedAt;
}
