package com.work.assistant.biz.password.metadata;

import lombok.Data;

/**
 * 密码本条目响应。
 *
 * <p>{@code password} 为服务端用 RSA 公钥加密后的 Base64 密文，
 * 客户端收到后需用「与服务端协商的私钥」解密（见前端 service 实现）。
 *
 * @author work-assistant
 */
@Data
public class PasswordEntryResponse {

  /**
   * 主键
   */
  private String id;

  /**
   * 名称
   */
  private String name;

  /**
   * 分类
   */
  private String category;

  /**
   * 账号 / 用户名
   */
  private String account;

  /**
   * 密码（RSA 公钥加密后的 Base64 密文）
   */
  private String password;

  /**
   * 网站地址
   */
  private String url;

  /**
   * 备注
   */
  private String remark;

  /**
   * 更新时间
   */
  private String updatedAt;
}
