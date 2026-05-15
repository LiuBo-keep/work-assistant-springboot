package com.work.assistant.config.retrofit;

import okhttp3.ConnectionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author aidan.liu
 */
@Configuration
public class RetrofitConfig {

  /**
   * 保障整个系统范围内就一个okhttp 连接池.
   */
  @Bean
  public ConnectionPool getConnectionPool() {
    return new ConnectionPool();
  }
}
