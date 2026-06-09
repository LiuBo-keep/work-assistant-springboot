package com.work.assistant.config.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/6/3 11:26
 */
@Component
public class IpRateLimiter {

  /**
   * IP缓存
   */
  private final ConcurrentHashMap<String, IpCounter> cache =
      new ConcurrentHashMap<>();

  public boolean allow(String ip) {

    long now = System.currentTimeMillis();
    long currentSecond = now / 1000;

    IpCounter counter = cache.get(ip);

    if (counter == null) {

      /*
       * 防止恶意IP撑爆Map
       */
      if (cache.size() >= RateLimitConstants.MAX_IP_CACHE_SIZE) {
        return false;
      }

      counter = cache.computeIfAbsent(ip, key -> {
        IpCounter c = new IpCounter();
        c.setCurrentSecond(currentSecond);
        c.setLastAccessTime(now);
        return c;
      });
    }

    synchronized (counter) {
      counter.setLastAccessTime(now);
      if (counter.getCurrentSecond() != currentSecond) {
        counter.setCurrentSecond(currentSecond);
        counter.getCounter().set(0);
      }
      return counter.getCounter()
          .incrementAndGet()
          <= RateLimitConstants.MAX_REQUEST_PER_SECOND;
    }
  }

  public void cleanExpiredIp() {
    long now = System.currentTimeMillis();
    for (Map.Entry<String, IpCounter> entry : cache.entrySet()) {
      IpCounter counter = entry.getValue();
      if (now - counter.getLastAccessTime() > RateLimitConstants.EXPIRE_MILLIS) {
        cache.remove(entry.getKey(), counter);
      }
    }
  }

  public int currentIpCount() {
    return cache.size();
  }
}
