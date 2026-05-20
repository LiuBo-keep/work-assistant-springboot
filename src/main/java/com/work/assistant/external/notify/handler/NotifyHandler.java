package com.work.assistant.external.notify.handler;

import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;

/**
 * 通知处理器
 */
public interface NotifyHandler {

  /**
   * 获取通知类型
   */
  NotifyType getNotifyType();

  /**
   * 执行通知
   */
  NotifyResponse notify(NotifyRequest request);
}
