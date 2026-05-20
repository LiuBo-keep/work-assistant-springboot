package com.work.assistant.external.notify.handler.impl.dingtalk.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DingTalkMsgType {

  @JsonProperty("text")
  TEXT,

  @JsonProperty("markdown")
  MARKDOWN
}
