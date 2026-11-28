package com.work.assistant.biz.user.metadata;

import lombok.Data;

/**
 * 修改本人密码请求。
 *
 * <p>由当前登录用户发起，需提供旧密码校验身份，
 * 新密码经服务端 AES 加密后覆盖落库。
 *
 * @author work-assistant
 */
@Data
public class ChangePasswordRequest {

  /**
   * 旧密码（明文，用于身份校验）。
   */
  private String oldPassword;

  /**
   * 新密码（明文，服务端 AES 加密落库）。
   */
  private String newPassword;
}
