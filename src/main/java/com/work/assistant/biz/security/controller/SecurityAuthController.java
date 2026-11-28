package com.work.assistant.biz.security.controller;

import com.work.assistant.biz.security.metadata.SecurityAuthRequest;
import com.work.assistant.biz.security.metadata.SecurityAuthResponse;
import com.work.assistant.biz.user.model.SysUser;
import com.work.assistant.biz.user.service.UserService;
import com.work.assistant.common.security.TokenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录鉴权控制器。
 *
 * <p>根据用户名与密码从数据库校验（{@link UserService#authenticate}），
 * 校验通过后签发携带角色的登录令牌，供前端做目录级权限控制。
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/token")
public class SecurityAuthController {

  private final UserService userService;

  public SecurityAuthController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 登录并签发令牌。
   *
   * @param request 用户名与密码
   * @return 令牌、角色与显示名称
   */
  @PostMapping
  public SecurityAuthResponse getToken(@RequestBody SecurityAuthRequest request)
      throws com.work.assistant.common.exception.WorkAssistantException {
    SysUser user = userService.authenticate(request.getUserName(), request.getPassword());

    SecurityAuthResponse response = new SecurityAuthResponse();
    response.setToken(TokenUtils.issueToken(user.getUserName(), user.getRole().name()));
    response.setRole(user.getRole().name());
    response.setDisplayName(user.getDisplayName());
    return response;
  }
}
