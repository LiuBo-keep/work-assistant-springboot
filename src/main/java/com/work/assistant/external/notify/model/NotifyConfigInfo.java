package com.work.assistant.external.notify.model;

import lombok.Data;

/**
 * 通知渠道配置信息。
 *
 * <p>聚合各通知渠道（钉钉、PushPlus）所需的连接配置。
 *
 * @author work-assistant
 */
@Data
public class NotifyConfigInfo {

  private String dingTalkUrl;

  private String pushPlusUrl;

  private String pushPlusToken;
}
