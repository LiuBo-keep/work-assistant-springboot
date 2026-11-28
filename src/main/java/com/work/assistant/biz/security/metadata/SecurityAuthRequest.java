package com.work.assistant.biz.security.metadata;

import lombok.Data;

/**
 * 登录鉴权请求。
 *
 * @author work-assistant
 */
@Data
public class SecurityAuthRequest {

  private String userName;

  private String password;
}