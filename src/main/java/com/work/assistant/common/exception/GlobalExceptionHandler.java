package com.work.assistant.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>统一将业务异常与未捕获异常转换为 {@link FaultResponse}，
 * 使前端可以稳定地通过 {@code response.data.error.code} 判断业务错误。
 *
 * @author aidan.liu
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 业务异常。
   */
  @ExceptionHandler(WorkAssistantException.class)
  public ResponseEntity<FaultResponse> handleWorkAssistantException(WorkAssistantException ex) {
    log.warn("business exception: {}", ex.getMessage());
    return build(ex.getErrorCode() == null
        ? HttpStatus.INTERNAL_SERVER_ERROR
        : ex.getErrorCode().getRefHttpStatusCode(), ex);
  }

  /**
   * 参数/请求体解析异常，统一归为 400。
   */
  @ExceptionHandler({
      IllegalArgumentException.class,
      org.springframework.http.converter.HttpMessageNotReadableException.class
  })
  public ResponseEntity<FaultResponse> handleBadRequest(Exception ex) {
    log.warn("bad request: {}", ex.getMessage());
    FaultResponse fault = FaultResponse.builder()
        .error(FaultResponse.Error.builder()
            .code(AppErrorCode.INTERNAL_SERVER_ERROR.name())
            .message("Bad request: " + ex.getMessage())
            .build())
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fault);
  }

  /**
   * 兜底异常。
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<FaultResponse> handleException(Exception ex) {
    log.error("unexpected exception", ex);
    FaultResponse fault = FaultResponse.builder()
        .error(FaultResponse.Error.builder()
            .code(AppErrorCode.INTERNAL_SERVER_ERROR.name())
            .message("Server side error, please try again later")
            .build())
        .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fault);
  }

  private ResponseEntity<FaultResponse> build(HttpStatus status, WorkAssistantException ex) {
    FaultResponse fault = new FaultResponse(ex);
    return ResponseEntity.status(status).body(fault);
  }
}
