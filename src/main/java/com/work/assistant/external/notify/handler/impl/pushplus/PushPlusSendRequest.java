package com.work.assistant.external.notify.handler.impl.pushplus;

import com.work.assistant.external.notify.enums.NotifyType;
import lombok.Data;

/**
 * PushPlus 推送请求体。
 *
 * @author aidan.liu
 */
@Data
public class PushPlusSendRequest {

  private String token;

  private String title;

  private String content;

  private String template;
}
