package com.work.assistant.biz.security.metadata;

import lombok.Data;

/**
 * 登录鉴权响应。
 *
 * @author work-assistant
 */
@Data
public class SecurityAuthResponse {

  /**
   * 访问令牌（含用户名与角色）。
   */
  private String token;

  /**
   * 用户角色：NORMAL / ADMIN。
   */
  private String role;

  /**
   * 显示名称 / 昵称。
   */
  private String displayName;
}
