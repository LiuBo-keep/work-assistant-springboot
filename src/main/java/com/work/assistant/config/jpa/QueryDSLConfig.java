package com.work.assistant.config.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author aidan.liu
 */
@Configuration
public class QueryDSLConfig {
  @Autowired
  private EntityManager entityManager;

  @Bean
  public JPAQueryFactory getQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }
}
