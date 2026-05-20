package com.work.assistant.external.notify.model;

import lombok.Data;

@Data
public class NotifyConfigInfo {

  private String dingTalkUrl;

  private String pushPlusUrl;

  private String pushPlusToken;
}
