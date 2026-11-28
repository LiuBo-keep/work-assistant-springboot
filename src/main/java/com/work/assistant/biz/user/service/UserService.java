package com.work.assistant.biz.user.service;

import com.work.assistant.biz.user.metadata.UserInfoResponse;
import com.work.assistant.biz.user.metadata.UserSaveRequest;
import com.work.assistant.biz.user.model.SysUser;
import com.work.assistant.biz.user.model.UserRole;
import com.work.assistant.biz.user.repository.UserRepository;
import com.work.assistant.common.exception.AppErrorCode;
import com.work.assistant.common.exception.WorkAssistantException;
import com.work.assistant.common.utils.AesEncryptorUtils;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务。
 *
 * <p>负责登录校验、用户增删改查与初始超级管理员初始化。
 * 密码在落库前经服务端 AES 加密，登录时用密文比对校验。
 *
 * @author work-assistant
 */
@Slf4j
@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * 应用启动时初始化默认超级管理员（仅当用户表为空时创建）。
   */
  @PostConstruct
  public void initDefaultAdmin() {
    try {
      if (userRepository.count() > 0) {
        return;
      }
      SysUser admin = new SysUser();
      admin.setUserName("admin");
      admin.setPassword(AesEncryptorUtils.encrypt("admin123"));
      admin.setDisplayName("超级管理员");
      admin.setRole(UserRole.ADMIN);
      admin.setEnabled(true);
      admin.setCreatedAt(LocalDateTime.now());
      admin.setUpdatedAt(LocalDateTime.now());
      userRepository.save(admin);
      log.info("default admin user initialized: admin/admin123");
    } catch (Exception e) {
      log.warn("init default admin failed", e);
    }
  }

  /**
   * 登录校验。
   *
   * @param userName 用户名
   * @param password 明文密码
   * @return 校验通过返回用户
   * @throws WorkAssistantException 凭证无效（401）或账号被禁用（403）
   */
  public SysUser authenticate(String userName, String password) throws WorkAssistantException {
    if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
      throw new WorkAssistantException(AppErrorCode.UNAUTHORIZED, "用户名或密码不能为空");
    }
    SysUser user = userRepository.findByUserName(userName.trim())
        .orElseThrow(() -> new WorkAssistantException(AppErrorCode.UNAUTHORIZED, "用户名或密码错误"));

    if (!Boolean.TRUE.equals(user.getEnabled())) {
      throw new WorkAssistantException(AppErrorCode.ACCOUNT_DISABLED, "账号已被禁用，请联系管理员");
    }

    // 明文密码 AES 加密后与落库密文比对
    String encrypted = AesEncryptorUtils.encrypt(password);
    if (encrypted == null || !encrypted.equals(user.getPassword())) {
      throw new WorkAssistantException(AppErrorCode.UNAUTHORIZED, "用户名或密码错误");
    }
    return user;
  }

  /**
   * 查询当前用户信息。
   */
  public UserInfoResponse currentUser(String userName) {
    SysUser user = userRepository.findByUserName(userName).orElse(null);
    if (user == null) {
      return null;
    }
    return toInfo(user);
  }

  /**
   * 查询全部用户（仅超管）。
   */
  public List<UserInfoResponse> list() {
    return userRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toInfo)
        .toList();
  }

  /**
   * 新增用户（仅超管）。
   */
  @Transactional(rollbackOn = Exception.class)
  public UserInfoResponse create(UserSaveRequest request) {
    validate(request, null);
    if (userRepository.existsByUserName(request.getUserName().trim())) {
      throw new IllegalArgumentException("用户名已存在");
    }

    SysUser user = new SysUser();
    user.setUserName(request.getUserName().trim());
    user.setPassword(AesEncryptorUtils.encrypt(request.getPassword()));
    user.setDisplayName(StringUtils.defaultIfBlank(request.getDisplayName(), request.getUserName()));
    user.setRole(parseRole(request.getRole()));
    user.setEnabled(request.getEnabled() == null || request.getEnabled());
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return toInfo(userRepository.save(user));
  }

  /**
   * 修改用户（仅超管）。
   */
  @Transactional(rollbackOn = Exception.class)
  public UserInfoResponse update(UserSaveRequest request) {
    validate(request, request.getId());
    SysUser user = userRepository.findById(request.getId())
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    if (StringUtils.isNotBlank(request.getUserName())
        && !request.getUserName().trim().equals(user.getUserName())
        && userRepository.existsByUserName(request.getUserName().trim())) {
      throw new IllegalArgumentException("用户名已存在");
    }

    if (StringUtils.isNotBlank(request.getUserName())) {
      user.setUserName(request.getUserName().trim());
    }
    if (StringUtils.isNotBlank(request.getPassword())) {
      user.setPassword(AesEncryptorUtils.encrypt(request.getPassword()));
    }
    if (request.getDisplayName() != null) {
      user.setDisplayName(StringUtils.defaultIfBlank(request.getDisplayName(), user.getUserName()));
    }
    if (request.getRole() != null) {
      user.setRole(parseRole(request.getRole()));
    }
    if (request.getEnabled() != null) {
      user.setEnabled(request.getEnabled());
    }
    user.setUpdatedAt(LocalDateTime.now());
    return toInfo(userRepository.save(user));
  }

  /**
   * 删除用户（仅超管，不允许删除自己）。
   */
  @Transactional(rollbackOn = Exception.class)
  public void delete(String id, String currentUserName) {
    SysUser user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    if (user.getUserName().equals(currentUserName)) {
      throw new IllegalArgumentException("不能删除当前登录账号");
    }
    userRepository.delete(user);
  }

  /**
   * 修改当前登录用户自己的密码。
   *
   * <p>需校验旧密码正确后方可更新，防止越权改密。
   *
   * @param userName 当前登录用户名
   * @param oldPassword 旧密码（明文）
   * @param newPassword 新密码（明文）
   * @throws WorkAssistantException 旧密码错误（401）
   */
  @Transactional(rollbackOn = Exception.class)
  public void changePassword(String userName, String oldPassword, String newPassword)
      throws WorkAssistantException {
    if (StringUtils.isBlank(oldPassword)) {
      throw new IllegalArgumentException("旧密码不能为空");
    }
    if (StringUtils.isBlank(newPassword)) {
      throw new IllegalArgumentException("新密码不能为空");
    }

    SysUser user = userRepository.findByUserName(userName)
        .orElseThrow(() -> new WorkAssistantException(AppErrorCode.UNAUTHORIZED, "用户不存在"));

    // 校验旧密码
    String encryptedOld = AesEncryptorUtils.encrypt(oldPassword);
    if (encryptedOld == null || !encryptedOld.equals(user.getPassword())) {
      throw new WorkAssistantException(AppErrorCode.UNAUTHORIZED, "旧密码错误");
    }

    // 新密码与旧密码相同则无需更新
    String encryptedNew = AesEncryptorUtils.encrypt(newPassword);
    if (encryptedNew != null && encryptedNew.equals(user.getPassword())) {
      throw new IllegalArgumentException("新密码不能与旧密码相同");
    }

    user.setPassword(encryptedNew);
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
    log.info("user [{}] changed password", userName);
  }

  private void validate(UserSaveRequest request, String id) {
    if (id == null) {
      // 新增：用户名与密码必填
      if (StringUtils.isBlank(request.getUserName())) {
        throw new IllegalArgumentException("用户名不能为空");
      }
      if (StringUtils.isBlank(request.getPassword())) {
        throw new IllegalArgumentException("密码不能为空");
      }
      if (request.getRole() == null) {
        throw new IllegalArgumentException("角色不能为空");
      }
    } else {
      if (StringUtils.isBlank(request.getId())) {
        throw new IllegalArgumentException("用户 ID 不能为空");
      }
    }
  }

  private UserRole parseRole(String role) {
    if (role == null) {
      return null;
    }
    try {
      return UserRole.valueOf(role.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("非法角色: " + role);
    }
  }

  private UserInfoResponse toInfo(SysUser user) {
    UserInfoResponse info = new UserInfoResponse();
    info.setId(user.getId());
    info.setUserName(user.getUserName());
    info.setDisplayName(user.getDisplayName());
    info.setRole(user.getRole() == null ? null : user.getRole().name());
    info.setEnabled(user.getEnabled());
    if (user.getCreatedAt() != null) {
      info.setCreatedAt(user.getCreatedAt().toString());
    }
    if (user.getUpdatedAt() != null) {
      info.setUpdatedAt(user.getUpdatedAt().toString());
    }
    return info;
  }
}
