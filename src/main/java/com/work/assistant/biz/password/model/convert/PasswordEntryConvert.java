package com.work.assistant.biz.password.model.convert;

import com.work.assistant.biz.password.metadata.PasswordEntrySaveRequest;
import com.work.assistant.biz.password.model.PasswordEntry;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * 密码本实体转换器。
 *
 * <p>注意：密码字段的加解密不在此转换器内完成，
 * 由服务层在「RSA 传输密文 ↔ 明文 ↔ 服务端 AES 落库」之间协调。
 * 转换器只负责普通字段映射。
 *
 * @author work-assistant
 */
@Component
public class PasswordEntryConvert {

  /**
   * 从新增请求创建实体（普通字段）。
   */
  public PasswordEntry fromCreateRequest(PasswordEntrySaveRequest request) {
    PasswordEntry entry = new PasswordEntry();
    entry.setId(blankToNull(request.getId()));
    apply(entry, request);
    entry.setCreatedAt(LocalDateTime.now());
    entry.setUpdatedAt(LocalDateTime.now());
    return entry;
  }

  /**
   * 将请求普通字段写入实体（新增/修改共用，不含密码与时间）。
   */
  public void apply(PasswordEntry entry, PasswordEntrySaveRequest request) {
    entry.setName(request.getName());
    entry.setCategory(blankToNull(request.getCategory()));
    entry.setAccount(request.getAccount());
    entry.setUrl(blankToNull(request.getUrl()));
    entry.setRemark(blankToNull(request.getRemark()));
  }

  private String blankToNull(String value) {
    return (value == null || value.trim().isEmpty()) ? null : value;
  }
}
