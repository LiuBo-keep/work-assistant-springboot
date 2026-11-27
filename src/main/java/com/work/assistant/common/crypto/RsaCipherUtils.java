package com.work.assistant.common.crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * RSA 非对称加解密工具。
 *
 * <p>用于密码在客户端与服务端之间传输时的非对称加密：
 * <ul>
 *   <li>客户端持有服务端下发的公钥，用公钥加密密码后上传；</li>
 *   <li>服务端持有私钥，用私钥解密得到明文，再做服务端 AES 落库加密；</li>
 *   <li>读取密码时，服务端用公钥加密明文后下发，客户端用「会话公钥对应的私钥」
 *       解密（本实现采用服务端下发明文对应的方式，见服务层说明）。</li>
 * </ul>
 *
 * <p>算法固定为 {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding}，密钥长度 2048 位。
 *
 * @author work-assistant
 */
public final class RsaCipherUtils {

  /**
   * RSA 密钥长度。
   */
  public static final int KEY_SIZE = 2048;

  /**
   * 加密/解密使用的填充算法。使用通用 {@code OAEPPadding}，
   * 具体的 OAEP 主哈希与 MGF1 哈希通过 {@link #OAEP_SPEC} 显式指定，
   * 以与浏览器 Web Crypto 的 {@code RSA-OAEP + SHA-256} 严格对齐。
   */
  private static final String CIPHER_ALGORITHM = "RSA/ECB/OAEPPadding";

  /**
   * OAEP 参数：主哈希 SHA-256，MGF1 掩码函数哈希 SHA-256。
   *
   * <p>注意：不能用 {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding} 简写，
   * 因为在部分 JDK 实现里它的 MGF1 会默认回退到 SHA-1，导致与浏览器
   * Web Crypto 的 OAEP-SHA256 不兼容，出现解密 padding 错误。
   */
  private static final OAEPParameterSpec OAEP_SPEC = new OAEPParameterSpec(
      "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

  private RsaCipherUtils() {
  }

  /**
   * 生成 RSA 密钥对。
   */
  public static KeyPair generateKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(KEY_SIZE);
      return generator.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("failed to generate RSA key pair", e);
    }
  }

  /**
   * 用公钥加密明文，返回 Base64 密文。
   */
  public static String encrypt(PublicKey publicKey, String plaintext) {
    try {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP_SPEC);
      byte[] encrypted = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      throw new IllegalStateException("failed to RSA encrypt", e);
    }
  }

  /**
   * 用私钥解密密文（Base64），返回明文。
   */
  public static String decrypt(PrivateKey privateKey, String ciphertext) {
    try {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SPEC);
      byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
      return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("failed to RSA decrypt", e);
    }
  }

  /**
   * 将公钥编码为 Base64（X.509 格式），供下发客户端。
   */
  public static String publicKeyToBase64(PublicKey publicKey) {
    return Base64.getEncoder().encodeToString(publicKey.getEncoded());
  }

  /**
   * 将私钥编码为 Base64（PKCS#8 格式），供持久化。
   */
  public static String privateKeyToBase64(PrivateKey privateKey) {
    return Base64.getEncoder().encodeToString(privateKey.getEncoded());
  }

  /**
   * 从 Base64 恢复公钥。
   */
  public static PublicKey publicKeyFromBase64(String base64) {
    try {
      byte[] decoded = Base64.getDecoder().decode(base64);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
      return KeyFactory.getInstance("RSA").generatePublic(spec);
    } catch (Exception e) {
      throw new IllegalStateException("failed to restore RSA public key", e);
    }
  }

  /**
   * 从 Base64 恢复私钥。
   */
  public static PrivateKey privateKeyFromBase64(String base64) {
    try {
      byte[] decoded = Base64.getDecoder().decode(base64);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
      return KeyFactory.getInstance("RSA").generatePrivate(spec);
    } catch (Exception e) {
      throw new IllegalStateException("failed to restore RSA private key", e);
    }
  }
}
