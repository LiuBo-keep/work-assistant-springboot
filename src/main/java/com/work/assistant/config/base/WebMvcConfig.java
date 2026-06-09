package com.work.assistant.config.base;

import com.work.assistant.config.ratelimit.RateLimitInterceptor;
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

  @Override
  public void addInterceptors(
      InterceptorRegistry registry) {

    registry.addInterceptor(rateLimitInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/actuator/**",
            "/error");
  }
}