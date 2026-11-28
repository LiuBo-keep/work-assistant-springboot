package com.work.assistant.biz.password.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RSA 公钥下发响应。
 *
 * @author work-assistant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {

  /**
   * 算法标识，固定为 {@code RSA}。
   */
  private String algorithm;

  /**
   * 公钥 Base64（X.509 编码）。
   */
  private String publicKey;
}
