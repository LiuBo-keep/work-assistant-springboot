package com.work.assistant.common.exception;

import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author aidan.liu
 */
@Slf4j
public class WorkAssistantException extends Exception {

  private static final long serialVersionUID = -3025433693788843913L;

  @Getter
  private AppErrorCode errorCode;

  @Getter
  private String errorMsg;

  @Getter
  private List<FaultResponse.Details> details;

  public WorkAssistantException() {

    super();
  }

  /**
   * @param errorCode 只传入errorCode，使用errorCode内部定义的参考的错误消息
   */
  public WorkAssistantException(AppErrorCode errorCode) {

    this.errorCode = errorCode;
  }

  /**
   * @param errorCode errorCode，
   * @param errorMsg  自定义errorMsg，不使用errorCode的参考错误消息
   */
  public WorkAssistantException(AppErrorCode errorCode, String errorMsg) {

    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
  }

  public WorkAssistantException(AppErrorCode errorCode, String errorMsg, List<FaultResponse.Details> details) {

    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
    this.details = details;
  }

  /**
   * @param message
   */
  public WorkAssistantException(String message) {

    this.errorMsg = message;
  }


  @Override
  public void printStackTrace() {

    log.error("{}-{}", errorCode, errorMsg);
  }

  @Override
  public String getMessage() {

    return "errorCode:" +
        (errorCode == null ? "" : errorCode) +
        "," + "errorMsg:" +
        (errorMsg == null ? "" : errorMsg);
  }
}

