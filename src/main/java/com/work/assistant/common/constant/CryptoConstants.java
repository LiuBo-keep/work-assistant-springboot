package com.work.assistant.common.constant;

/**
 * 加解密相关常量。
 *
 * <p>集中管理服务端加解密所需的密钥等常量，避免在多处硬编码导致的不一致风险。
 * AES 对称加密与登录令牌签名共用同一密钥，保证令牌与服务端绑定。
 *
 * @author work-assistant
 */
public final class CryptoConstants {

  /**
   * 服务端 AES 加密密钥与登录令牌签名密钥（应通过配置或密钥管理服务下发，勿在代码中明文硬编码）。
   */
  public static final String SERVER_SECRET = "6c96439b-ad24-4428-81ff-184d386afe28";

  private CryptoConstants() {
  }
}
