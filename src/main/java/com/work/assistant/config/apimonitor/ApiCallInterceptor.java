package com.work.assistant.config.apimonitor;

import com.work.assistant.biz.monitor.model.ApiCallLog;
import com.work.assistant.biz.monitor.repository.ApiCallLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 接口调用记录拦截器。
 *
 * <p>在 {@code afterCompletion} 阶段记录每次接口调用的方法、路径、状态码、耗时、
 * 客户端 IP 与用户，供「接口监控」子页面统计。
 *
 * <p>为避免「监控接口监控自身」造成写放大，会跳过 {@code /api/v1/monitor/**} 路径，
 * 并仅记录 {@code /api/**} 业务接口（静态资源、错误页等不记录）。
 *
 * @author work-assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallInterceptor implements HandlerInterceptor {

  private static final String START_TIME_ATTR = ApiCallInterceptor.class.getName() + ".startTime";
  private static final String MONITOR_PATH_PREFIX = "/api/v1/monitor/";

  private final ApiCallLogRepository apiCallLogRepository;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                              Exception ex) {
    try {
      String path = request.getRequestURI();
      // 只记录业务接口，跳过监控接口自身与静态资源，避免自监控死循环与写放大
      if (!path.startsWith("/api/") || path.startsWith(MONITOR_PATH_PREFIX)) {
        return;
      }

      long start = (long) request.getAttribute(START_TIME_ATTR);
      long durationMs = System.currentTimeMillis() - start;

      ApiCallLog callLog = new ApiCallLog();
      callLog.setHttpMethod(request.getMethod());
      callLog.setRequestPath(path);
      callLog.setStatusCode(response.getStatus());
      callLog.setDurationMs(durationMs);
      callLog.setClientIp(getClientIp(request));
      Object userName = request.getAttribute("authUserName");
      callLog.setUserName(userName == null ? null : userName.toString());
      callLog.setCreatedAt(LocalDateTime.now());

      apiCallLogRepository.save(callLog);
    } catch (Exception e) {
      // 记录失败不影响业务请求
      log.warn("record api call log failed: {}", e.getMessage());
    }
  }

  private String getClientIp(HttpServletRequest request) {
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
