package com.work.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WorkAssistantApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkAssistantApplication.class, args);
  }
}
