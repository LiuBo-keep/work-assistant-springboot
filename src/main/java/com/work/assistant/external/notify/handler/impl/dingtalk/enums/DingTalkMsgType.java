package com.work.assistant.external.notify.handler.impl.dingtalk.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 钉钉消息类型。
 *
 * @author work-assistant
 */
public enum DingTalkMsgType {

  @JsonProperty("text")
  TEXT,

  @JsonProperty("markdown")
  MARKDOWN
}
