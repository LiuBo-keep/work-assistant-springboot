package com.work.assistant.config.base;

import com.work.assistant.config.ratelimit.RateLimitInterceptor;
import com.work.assistant.config.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:28
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig
    implements WebMvcConfigurer {

  private final RateLimitInterceptor rateLimitInterceptor;
  private final AuthInterceptor authInterceptor;

  @Override
  public void addInterceptors(
      InterceptorRegistry registry) {

    // 全局限流：最外层，先于鉴权执行。
    // 注意：/actuator/** 不再放行，纳入限流范围，防止匿名高频拉取指标。
    registry.addInterceptor(rateLimitInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/error");

    // 鉴权：放行登录接口与静态资源，其余接口需携带有效令牌。
    // /actuator/** 需要鉴权，避免系统运行指标被匿名访问。
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/api/v1/token",
            "/error",
            "/favicon.ico",
            "/index.html",
            "/assets/**",
            "/static/**");
  }
}