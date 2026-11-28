package com.work.assistant.external.notify.handler;

import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;

/**
 * 通知处理器。
 *
 * <p>不同通知渠道（钉钉、PushPlus 等）实现该接口，由 {@code NotifyManager}
 * 按 {@link NotifyType} 路由分发。
 *
 * @author work-assistant
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
