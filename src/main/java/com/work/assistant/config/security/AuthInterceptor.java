package com.work.assistant.config.security;

import com.work.assistant.common.security.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器。
 *
 * <p>校验请求头 {@code Authorization: Bearer <token>}，无效则返回 401。
 * 白名单由 {@code WebMvcConfig} 注册时通过 {@code excludePathPatterns} 控制，
 * 本类仅负责令牌校验，保持职责单一。
 *
 * @author aidan.liu
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    // CORS 预检请求直接放行。
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    String authorization = request.getHeader("Authorization");
    String token = null;
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      token = authorization.substring(BEARER_PREFIX.length()).trim();
    }

    String userName = TokenUtils.verifyAndGetUserName(token);
    if (userName == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write(
          "{\"code\":401,\"message\":\"Unauthorized\"}");
      return false;
    }

    request.setAttribute("authUserName", userName);
    return true;
  }
}
