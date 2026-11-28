package com.work.assistant.config.base;

import com.work.assistant.config.apimonitor.ApiCallInterceptor;
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
  private final ApiCallInterceptor apiCallInterceptor;

  @Override
  public void addInterceptors(
      InterceptorRegistry registry) {

    // 全局限流：最外层，先于鉴权执行。
    // 注意：/actuator/** 不再放行，纳入限流范围，防止匿名高频拉取指标。
    registry.addInterceptor(rateLimitInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/error");

    // 鉴权：仅对 /api 业务接口做校验，放行登录接口。
    // 静态资源（/、/index.html、/assets/**、/favicon.ico 等）不在此范围，
    // 白名单方式可避免遗漏静态路径导致首页被误拦截。
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/v1/token");

    // 接口调用记录：最后执行（可读取鉴权写入的 authUserName），仅记录 /api 业务接口。
    registry.addInterceptor(apiCallInterceptor)
        .addPathPatterns("/api/**");
  }
}