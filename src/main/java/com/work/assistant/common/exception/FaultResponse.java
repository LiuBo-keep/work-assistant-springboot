package com.work.assistant.common.exception;

import static com.work.assistant.common.exception.AppErrorCode.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aidan.liu
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultResponse {

  /**
   * 错误情况下才会返回。
   */
  public Error error;

  public FaultResponse(WorkAssistantException ex) {


    String errorCode = Optional.ofNullable(ex.getErrorCode())
        .map(Enum::name)
        .orElse(INTERNAL_SERVER_ERROR.name());

    String msg;
    if (StringUtils.isBlank(ex.getErrorMsg())) {
      msg = ex.getErrorCode().getMsg();
    } else {
      msg = ex.getErrorMsg();
    }

    List<Details> details;
    if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
      details = ex.getDetails();
    } else {
      details = null;
    }
    this.error = Error.builder().code(errorCode).message(msg).details(details).build();
  }

  @Builder
  @Data
  public static class Error {
    public String code;
    public String message;
    public List<Details> details;
  }

  /**
   * 多种异常情况下使用List<Details> details;
   * 一般可以不使用。
   */
  @Builder
  @Data
  public static class Details {
    public String code;
    public String message;
  }
}