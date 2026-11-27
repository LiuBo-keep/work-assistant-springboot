package com.work.assistant.biz.password.controller;

import com.work.assistant.biz.password.metadata.PasswordEntryResponse;
import com.work.assistant.biz.password.metadata.PasswordEntrySaveRequest;
import com.work.assistant.biz.password.metadata.PublicKeyResponse;
import com.work.assistant.biz.password.service.PasswordEntryService;
import com.work.assistant.common.crypto.RsaKeyManager;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密码本控制器。
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/password")
public class PasswordController {

  private final PasswordEntryService passwordEntryService;
  private final RsaKeyManager rsaKeyManager;

  public PasswordController(PasswordEntryService passwordEntryService,
                            RsaKeyManager rsaKeyManager) {
    this.passwordEntryService = passwordEntryService;
    this.rsaKeyManager = rsaKeyManager;
  }

  /**
   * 下发服务端 RSA 公钥（用于客户端加密上传密码）。
   */
  @GetMapping("/public-key")
  public PublicKeyResponse publicKey() {
    return new PublicKeyResponse("RSA", rsaKeyManager.getPublicKeyBase64());
  }

  /**
   * 查询当前用户全部密码条目。
   */
  @GetMapping("/list")
  public List<PasswordEntryResponse> list(
      @RequestAttribute("authUserName") String owner,
      @RequestHeader(value = "X-Client-Public-Key", required = false) String clientPublicKey) {
    return passwordEntryService.list(owner, clientPublicKey);
  }

  /**
   * 新增密码条目。
   */
  @PostMapping("/save")
  public PasswordEntryResponse save(
      @RequestAttribute("authUserName") String owner,
      @RequestBody PasswordEntrySaveRequest request) {
    if (request.getId() == null || request.getId().trim().isEmpty()) {
      return passwordEntryService.create(request, owner);
    }
    return passwordEntryService.update(request, owner);
  }

  /**
   * 删除密码条目。
   */
  @DeleteMapping("/{id}")
  public void delete(
      @RequestAttribute("authUserName") String owner,
      @PathVariable("id") String id) {
    passwordEntryService.delete(id, owner);
  }
}
