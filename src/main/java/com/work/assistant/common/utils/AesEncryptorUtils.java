package com.work.assistant.common.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.work.assistant.common.constant.CryptoConstants;
import io.micrometer.common.util.StringUtils;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 对称加密工具。
 *
 * <p>基于 JDK 内置 {@code AES/ECB/PKCS5Padding} 实现，密钥由 {@link CryptoConstants#SERVER_SECRET}
 * 派生。主要用于数据库敏感字段（如密码本密码）的落库加密与解密。
 *
 * <p>空白内容不加密，直接返回 {@code null}；加密/解密异常会抛出
 * {@link IllegalStateException} 而非静默吞掉，便于上层感知密钥或数据异常。
 *
 * @author work-assistant
 */
public final class AesEncryptorUtils {

  private static final String KEY_ALGORITHM = "AES";

  private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

  private AesEncryptorUtils() {
  }

  /**
   * 加密明文，返回 Base64 密文。
   *
   * @param content 明文；为空白时返回 {@code null}
   * @return Base64 密文，或 {@code null}（输入为空白时）
   * @throws IllegalStateException 加密失败时抛出
   */
  public static String encrypt(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }
    try {
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
      byte[] result = cipher.doFinal(content.getBytes(UTF_8));
      return Base64.getEncoder().encodeToString(result);
    } catch (Exception ex) {
      throw new IllegalStateException("AES encrypt failed", ex);
    }
  }

  /**
   * 解密 Base64 密文，返回明文。
   *
   * @param content Base64 密文；为空白时返回 {@code null}
   * @return 明文，或 {@code null}（输入为空白时）
   * @throws IllegalStateException 解密失败时抛出
   */
  public static String decrypt(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }
    try {
      Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
      byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));
      return new String(result, UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("AES decrypt failed", ex);
    }
  }

  /**
   * 由服务端密钥派生 AES 专用密钥（128 位）。
   */
  private static SecretKeySpec getSecretKey() {
    try {
      KeyGenerator generator = KeyGenerator.getInstance(KEY_ALGORITHM);
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(CryptoConstants.SERVER_SECRET.getBytes(UTF_8));
      generator.init(128, random);
      SecretKey secretKey = generator.generateKey();
      return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("AES key generation failed", ex);
    }
  }
}
