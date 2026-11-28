package com.work.assistant.external.notify.handler;

import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import com.work.assistant.biz.hrms.repository.HrmsNotifyConfigRepository;
import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.model.NotifyConfigInfo;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 通知配置提供者。
 *
 * <p>从通知配置表读取渠道配置（钉钉 Webhook、PushPlus Token 等），
 * 按通知类型组装为 {@link NotifyConfigInfo}。
 *
 * @author work-assistant
 */
@Component
public class NotifyConfigProvider {

  private final HrmsNotifyConfigRepository hrmsNotifyConfigRepository;

  public NotifyConfigProvider(HrmsNotifyConfigRepository hrmsNotifyConfigRepository) {
    this.hrmsNotifyConfigRepository = hrmsNotifyConfigRepository;
  }

  public NotifyConfigInfo getNotifyConfig(NotifyType notifyType) {

    List<HrmsNotifyConfig> all = hrmsNotifyConfigRepository.findAll();
    if (CollectionUtils.isEmpty(all) || Boolean.FALSE.equals(all.getFirst().getEnabled())) {
      return null;
    }

    NotifyConfigInfo notifyConfigInfo = new NotifyConfigInfo();
    HrmsNotifyConfig first = all.getFirst();
    if (NotifyType.DING_TALK.equals(notifyType)) {
      notifyConfigInfo.setDingTalkUrl(first.getDingTalkUrl());
    }
    if (NotifyType.PUSH_PLUS.equals(notifyType)) {
      notifyConfigInfo.setPushPlusUrl(first.getPushPlusUrl());
      notifyConfigInfo.setPushPlusToken(first.getPushPlusToken());
    }
    return notifyConfigInfo;
  }
}
