package com.work.assistant.biz.user.controller;

import com.work.assistant.biz.user.metadata.ChangePasswordRequest;
import com.work.assistant.biz.user.metadata.UserInfoResponse;
import com.work.assistant.biz.user.metadata.UserSaveRequest;
import com.work.assistant.biz.user.model.UserRole;
import com.work.assistant.biz.user.service.UserService;
import com.work.assistant.common.exception.WorkAssistantException;
import com.work.assistant.common.response.RestResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器。
 *
 * <p>除「当前用户信息」外，其余接口均要求超级管理员角色，
 * 由 {@link #requireAdmin(String)} 统一校验。
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 查询当前登录用户信息。
   */
  @GetMapping("/me")
  public RestResponse me(@RequestAttribute("authUserName") String userName) {
    UserInfoResponse info = userService.currentUser(userName);
    return RestResponse.success(info);
  }

  /**
   * 查询全部用户（仅超管）。
   */
  @GetMapping("/list")
  public RestResponse list(
      @RequestAttribute("authUserName") String userName,
      @RequestAttribute("authUserRole") String role) {
    requireAdmin(role);
    List<UserInfoResponse> list = userService.list();
    return RestResponse.success(list);
  }

  /**
   * 新增用户（仅超管）。
   */
  @PostMapping("/save")
  public RestResponse save(
      @RequestAttribute("authUserName") String userName,
      @RequestAttribute("authUserRole") String role,
      @RequestBody UserSaveRequest request) {
    requireAdmin(role);
    if (request.getId() == null || request.getId().trim().isEmpty()) {
      return RestResponse.success(userService.create(request));
    }
    return RestResponse.success(userService.update(request));
  }

  /**
   * 删除用户（仅超管，不允许删除自己）。
   */
  @DeleteMapping("/{id}")
  public RestResponse delete(
      @RequestAttribute("authUserName") String userName,
      @RequestAttribute("authUserRole") String role,
      @PathVariable("id") String id) {
    requireAdmin(role);
    userService.delete(id, userName);
    return RestResponse.success();
  }

  /**
   * 修改当前登录用户自己的密码（所有角色均可）。
   */
  @PostMapping("/change-password")
  public RestResponse changePassword(
      @RequestAttribute("authUserName") String userName,
      @RequestBody ChangePasswordRequest request) throws WorkAssistantException {
    userService.changePassword(userName, request.getOldPassword(), request.getNewPassword());
    return RestResponse.success();
  }

  /**
   * 校验当前用户是否为超级管理员。
   */
  private void requireAdmin(String role) {
    if (!UserRole.ADMIN.name().equals(role)) {
      throw new IllegalArgumentException("仅超级管理员可执行此操作");
    }
  }
}
