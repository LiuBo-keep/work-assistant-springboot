package com.work.assistant.config.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpCacheCleaner {

  private final IpRateLimiter ipRateLimiter;

  /**
   * 每分钟清理一次
   */
  @Scheduled(fixedDelay = 60_000)
  public void clean() {

    ipRateLimiter.cleanExpiredIp();

    log.info("Current IP Cache Size: {}",
        ipRateLimiter.currentIpCount());
  }
}
