package com.work.assistant.config.database;

import com.work.assistant.common.crypto.AesEncryptorHelper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * jpa 操作数据库属性加密
 *
 * @author aidan.liu
 */
@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {
  @Override
  public String convertToDatabaseColumn(String attribute) {
    return AesEncryptorHelper.encrypt(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    return AesEncryptorHelper.decrypt(dbData);
  }
}
