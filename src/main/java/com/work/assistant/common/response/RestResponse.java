package com.work.assistant.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.work.assistant.common.constant.Global;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.MDC;

/**
 * @author aidan.liu
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse {

  /**
   * 全局唯一请求ID（系统生成，无则赋空字符串） 必选字段
   */
  private String requestId;

  /**
   * 业务状态码（字符串，成功固定为"SUCCESS"，失败如"PMS_INVALID_PARAMETER"等） 必选字段
   */
  private String code;

  /**
   * 用户可见提示，长度≤100字符 必选字段
   */
  private String msg;

  /**
   * 响应数据，成功时返回具体数据，失败时为null 可选字段
   */
  private Object data;

  /**
   * 错误详情，仅失败时返回，成功时省略 数组格式支持多错误场景 可选字段
   */
  private List<ErrorDetail> errorDetails;

  /**
   * 服务端响应时间戳（毫秒） 必选字段
   */
  private Long timestamp;

  /**
   * 成功, data返回数据
   */
  public static RestResponse success() {
    return success(null, "success");
  }

  /**
   * 成功，data返回数据
   */
  public static RestResponse success(Object data) {
    return success(data, "success");
  }

  /**
   * 成功, data返回数据, 自定义返回提示信息
   */
  public static RestResponse success(Object data, String msg) {
    return RestResponse.builder()
        .requestId(MDC.get(Global.REQUEST_ID))
        .code("SUCCESS")
        .msg(msg)
        .data(data)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * 失败, 自定义错误码和提示信息
   */
  public static RestResponse fail(String code, String msg) {
    return RestResponse.builder()
        .requestId(MDC.get(Global.REQUEST_ID))
        .code(code)
        .msg(msg)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * 失败, 自定义错误码和提示信息, 以及错误详情
   */
  public static RestResponse fail(String code, String msg, List<ErrorDetail> errorDetails) {
    return RestResponse.builder()
        .requestId(MDC.get(Global.REQUEST_ID))
        .code(code)
        .msg(msg)
        .errorDetails(errorDetails)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * 错误详情，仅失败时返回，成功时省略
   */
  @Builder
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ErrorDetail {

    /**
     * 细分错误码（定位具体问题）
     */
    private String subCode;

    /**
     * 开发用详细错误描述，长度≤500字符
     */
    private String subMsg;
  }

}

