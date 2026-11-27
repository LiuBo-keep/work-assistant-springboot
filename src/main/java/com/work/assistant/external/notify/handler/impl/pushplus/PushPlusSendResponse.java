package com.work.assistant.external.notify.handler.impl.pushplus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * PushPlus 推送响应体。
 *
 * @author aidan.liu
 */
@Data
public class PushPlusSendResponse {

  @JsonProperty("code")
  private int code;

  @JsonProperty("msg")
  private String msg;

  @JsonProperty("data")
  private String data;
}
