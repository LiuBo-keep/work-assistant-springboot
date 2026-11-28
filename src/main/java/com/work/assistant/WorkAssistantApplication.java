package com.work.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动入口。
 *
 * <p>启用定时任务（{@link EnableScheduling}），负责装配 Spring Boot 应用上下文。
 *
 * @author work-assistant
 */
@EnableScheduling
@SpringBootApplication
public class WorkAssistantApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkAssistantApplication.class, args);
  }
}
