package com.work.assistant.config.ratelimit;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:25
 */
public final class RateLimitConstants {

  /**
   * 每秒允许请求次数
   */
  public static final int MAX_REQUEST_PER_SECOND = 3;

  /**
   * 最大IP缓存数
   */
  public static final int MAX_IP_CACHE_SIZE = 100_000;

  /**
   * 5分钟无访问自动清理
   */
  public static final long EXPIRE_MILLIS = 5 * 60 * 1000L;

  private RateLimitConstants() {
  }
}
