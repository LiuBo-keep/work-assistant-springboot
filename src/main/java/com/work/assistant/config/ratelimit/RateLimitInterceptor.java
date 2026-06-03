package com.work.assistant.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:27
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor
    implements HandlerInterceptor {

  private final IpRateLimiter ipRateLimiter;

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler) throws Exception {

    String ip = getClientIp(request);

    if (!ipRateLimiter.allow(ip)) {
      response.setStatus(429);
      response.setContentType(
          "application/json;charset=UTF-8");
      response.getWriter().write("""
          {
            "code":429,
            "message":"Too Many Requests"
          }
          """);
      return false;
    }

    return true;
  }

  private String getClientIp(
      HttpServletRequest request) {

    String ip = request.getHeader("X-Forwarded-For");

    if (ip != null && !ip.isBlank()) {
      int idx = ip.indexOf(",");
      return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
    }

    ip = request.getHeader("X-Real-IP");
    if (ip != null && !ip.isBlank()) {
      return ip;
    }
    return request.getRemoteAddr();
  }
}
