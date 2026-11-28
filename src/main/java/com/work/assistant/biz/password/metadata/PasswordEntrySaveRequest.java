package com.work.assistant.biz.password.metadata;

import lombok.Data;

/**
 * 密码本条目保存请求（新增/修改）。
 *
 * <p>{@code password} 为客户端使用服务端 RSA 公钥加密后的 Base64 密文；
 * 服务端用私钥解密后，再以服务端 AES 落库加密。
 *
 * @author work-assistant
 */
@Data
public class PasswordEntrySaveRequest {

  /**
   * 主键（新增时为空）
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
   * 密码（服务端 RSA 公钥加密后的 Base64 密文）
   */
  private String password;

  /**
   * 客户端 RSA 公钥 Base64（用于服务端加密密码明文后下发）
   */
  private String clientPublicKey;

  /**
   * 网站地址（可选）
   */
  private String url;

  /**
   * 备注（可选）
   */
  private String remark;
}
