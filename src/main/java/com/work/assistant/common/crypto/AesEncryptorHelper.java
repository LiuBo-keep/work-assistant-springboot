package com.work.assistant.common.crypto;

import com.work.assistant.common.utils.AesEncryptorUtils;

/**
 * AES 加密助手（委托实现）。
 *
 * <p>为避免与 {@link AesEncryptorUtils} 重复实现，本类仅作为其薄委托，
 * 供数据库字段加密转换器 {@code AesEncryptorConverter} 等场景使用。
 *
 * @author aidan.liu
 */
public final class AesEncryptorHelper {

  private AesEncryptorHelper() {
  }

  public static String encrypt(String content) {
    return AesEncryptorUtils.encrypt(content);
  }

  public static String decrypt(String content) {
    return AesEncryptorUtils.decrypt(content);
  }
}
