package com.work.assistant.biz.password.service;

import com.work.assistant.biz.password.metadata.PasswordEntryResponse;
import com.work.assistant.biz.password.metadata.PasswordEntrySaveRequest;
import com.work.assistant.biz.password.model.PasswordEntry;
import com.work.assistant.biz.password.model.convert.PasswordEntryConvert;
import com.work.assistant.biz.password.repository.PasswordEntryRepository;
import com.work.assistant.common.crypto.RsaCipherUtils;
import com.work.assistant.common.crypto.RsaKeyManager;
import jakarta.transaction.Transactional;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 密码本服务。
 *
 * <p>密码传输采用双向非对称加密，存储采用服务端对称加密：
 * <ul>
 *   <li><b>上传</b>：客户端用「服务端 RSA 公钥」加密密码 → 服务端私钥解密得到明文 →
 *       实体经 {@code AesEncryptorConverter} 落库时 AES 加密。</li>
 *   <li><b>下发</b>：实体读取时 AES 解密得到明文 → 服务端用「客户端 RSA 公钥」加密明文
 *       后下发，客户端用自己的私钥解密。</li>
 * </ul>
 *
 * <p>客户端公钥由前端在会话内临时生成，随请求以请求头
 * {@code X-Client-Public-Key} 或请求体 {@code clientPublicKey} 传入。
 *
 * @author work-assistant
 */
@Service
public class PasswordEntryService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final PasswordEntryRepository repository;
  private final PasswordEntryConvert convert;
  private final RsaKeyManager rsaKeyManager;

  public PasswordEntryService(PasswordEntryRepository repository,
                              PasswordEntryConvert convert,
                              RsaKeyManager rsaKeyManager) {
    this.repository = repository;
    this.convert = convert;
    this.rsaKeyManager = rsaKeyManager;
  }

  /**
   * 查询当前用户全部密码条目（密码用客户端公钥加密后下发）。
   *
   * @param owner           当前用户名
   * @param clientPublicKey 客户端 RSA 公钥 Base64（用于加密密码明文）
   */
  public List<PasswordEntryResponse> list(String owner, String clientPublicKey) {
    PublicKey clientKey = parseClientPublicKey(clientPublicKey);
    return repository.findByOwnerOrderByUpdatedAtDesc(owner).stream()
        .map(entry -> toResponse(entry, clientKey))
        .toList();
  }

  /**
   * 新增密码条目。
   */
  @Transactional(rollbackOn = Exception.class)
  public PasswordEntryResponse create(PasswordEntrySaveRequest request, String owner) {
    if (isBlank(request.getName())) {
      throw new IllegalArgumentException("名称不能为空");
    }

    PasswordEntry entry = convert.fromCreateRequest(request);
    entry.setOwner(owner);
    // 服务端私钥解密（上传方向）
    entry.setPassword(RsaCipherUtils.decrypt(rsaKeyManager.getPrivateKey(), request.getPassword()));
    repository.save(entry);
    return toResponse(entry, parseClientPublicKey(request.getClientPublicKey()));
  }

  /**
   * 修改密码条目。
   */
  @Transactional(rollbackOn = Exception.class)
  public PasswordEntryResponse update(PasswordEntrySaveRequest request, String owner) {
    if (isBlank(request.getId())) {
      throw new IllegalArgumentException("记录 id 不能为空");
    }
    PasswordEntry entry = repository.findById(request.getId())
        .filter(e -> owner.equals(e.getOwner()))
        .orElseThrow(() -> new IllegalArgumentException("记录不存在或无权访问"));

    if (isBlank(request.getName())) {
      throw new IllegalArgumentException("名称不能为空");
    }
    convert.apply(entry, request);
    // 仅当客户端传入了密码密文时才更新密码（兼容只改其他字段的场景）
    if (!isBlank(request.getPassword())) {
      entry.setPassword(
          RsaCipherUtils.decrypt(rsaKeyManager.getPrivateKey(), request.getPassword()));
    }
    entry.setUpdatedAt(LocalDateTime.now());
    repository.save(entry);
    return toResponse(entry, parseClientPublicKey(request.getClientPublicKey()));
  }

  /**
   * 删除密码条目。
   */
  @Transactional(rollbackOn = Exception.class)
  public void delete(String id, String owner) {
    if (isBlank(id)) {
      return;
    }
    repository.findById(id)
        .filter(e -> owner.equals(e.getOwner()))
        .ifPresent(repository::delete);
  }

  /**
   * 实体 → 响应（密码字段：明文 → 客户端公钥加密后下发）。
   */
  private PasswordEntryResponse toResponse(PasswordEntry entry, PublicKey clientPublicKey) {
    PasswordEntryResponse response = new PasswordEntryResponse();
    response.setId(entry.getId());
    response.setName(entry.getName());
    response.setCategory(entry.getCategory());
    response.setAccount(entry.getAccount());
    if (clientPublicKey != null && entry.getPassword() != null) {
      response.setPassword(
          RsaCipherUtils.encrypt(clientPublicKey, entry.getPassword()));
    } else {
      response.setPassword(null);
    }
    response.setUrl(entry.getUrl());
    response.setRemark(entry.getRemark());
    if (entry.getUpdatedAt() != null) {
      response.setUpdatedAt(entry.getUpdatedAt().format(DATE_TIME_FORMATTER));
    }
    return response;
  }

  private PublicKey parseClientPublicKey(String clientPublicKey) {
    if (isBlank(clientPublicKey)) {
      return null;
    }
    return RsaCipherUtils.publicKeyFromBase64(clientPublicKey);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
