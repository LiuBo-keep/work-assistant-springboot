package com.work.assistant.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author aidan.liu
 */
@Getter
public enum AppErrorCode {

  /**
   * 未知错误.
   */
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server side error, please try again later"),

  /**
   * 密码过期需求修改密码.
   */
  NEED_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "password need changed"),

  /**
   * 需要进行二次认证时返回前端的标识
   */
  LOGIN_2FA_AUTH(HttpStatus.BAD_REQUEST, "login 2fa auth"),

  /**
   * 登录失败 统一返回的错误信息
   */
  LOGIN_FAILED(HttpStatus.BAD_REQUEST,
      "Login failed. Possible reasons:Tenant name, login or password is wrong;"
          + "Account is locked due to failed login attempts;Account is inactive or blocked;"
          + "For more support please contact your administrator two-factor authentication failed");

  private final HttpStatus refHttpStatusCode;
  private final String msg;

  AppErrorCode(HttpStatus refHttpStatusCode, String msg) {
    this.refHttpStatusCode = refHttpStatusCode;
    this.msg = msg;
  }
}
