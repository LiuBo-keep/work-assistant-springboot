package com.work.assistant.config.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:25
 */
@Data
public class IpCounter {

  /**
   * 当前统计秒
   */
  private volatile long currentSecond;

  /**
   * 当前秒请求次数
   */
  private final AtomicInteger counter = new AtomicInteger(0);

  /**
   * 最后访问时间
   */
  private volatile long lastAccessTime;
}
