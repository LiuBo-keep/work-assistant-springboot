package com.work.assistant.biz.security.metadata;

import lombok.Data;

@Data
public class SecurityAuthRequest {

  private String userName;

  private String password;
}