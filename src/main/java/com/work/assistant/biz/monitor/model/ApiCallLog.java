package com.work.assistant.biz.monitor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接口调用日志。
 *
 * <p>记录每一次 HTTP 接口调用的元信息，用于「接口监控」子页面统计接口调用量、
 * 成功率、平均耗时、慢接口与状态码分布。
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "api_call_log")
public class ApiCallLog {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * HTTP 方法（GET/POST/PUT/DELETE 等）
   */
  @Column(name = "http_method")
  private String httpMethod;

  /**
   * 请求路径（如 /api/v1/monitor/overview）
   */
  @Column(name = "request_path")
  private String requestPath;

  /**
   * HTTP 状态码
   */
  @Column(name = "status_code")
  private Integer statusCode;

  /**
   * 耗时（毫秒）
   */
  @Column(name = "duration_ms")
  private Long durationMs;

  /**
   * 客户端 IP
   */
  @Column(name = "client_ip")
  private String clientIp;

  /**
   * 调用用户（未鉴权时为空）
   */
  @Column(name = "user_name")
  private String userName;

  /**
   * 记录时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
