package com.work.assistant.biz.hrms.metadata.convert;

import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigQueryResponse;
import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Component
public class ToHrmsNotifyConfigQueryResponseConvert {

  public HrmsNotifyConfigQueryResponse fromHrmsNotifyConfig(HrmsNotifyConfig hrmsNotifyConfig) {
    HrmsNotifyConfigQueryResponse hrmsNotifyConfigQueryResponse = new HrmsNotifyConfigQueryResponse();
    hrmsNotifyConfigQueryResponse.setId(hrmsNotifyConfig.getId());
    hrmsNotifyConfigQueryResponse.setNotifyChannel(hrmsNotifyConfig.getNotifyChannel());
    hrmsNotifyConfigQueryResponse.setEnabled(hrmsNotifyConfig.getEnabled());
    hrmsNotifyConfigQueryResponse.setPushPlusUrl(hrmsNotifyConfig.getPushPlusUrl());
    hrmsNotifyConfigQueryResponse.setPushPlusToken(hrmsNotifyConfig.getPushPlusToken());
    hrmsNotifyConfigQueryResponse.setDingTalkUrl(hrmsNotifyConfig.getDingTalkUrl());
    hrmsNotifyConfigQueryResponse.setHrmsUrl(hrmsNotifyConfig.getHrmsUrl());
    hrmsNotifyConfigQueryResponse.setHrmsToken(hrmsNotifyConfig.getHrmsToken());
    return hrmsNotifyConfigQueryResponse;
  }
}
