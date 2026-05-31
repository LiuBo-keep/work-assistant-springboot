package com.work.assistant.biz.hrms.model.hrms.convert;

import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigSaveRequest;
import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Component
public class HrmsNotifyConfigConvert {

    public HrmsNotifyConfig fromHrmsNotifyConfigCreateRequest(HrmsNotifyConfigSaveRequest request) {
        HrmsNotifyConfig hrmsNotifyConfig = new HrmsNotifyConfig();
        hrmsNotifyConfig.setId(request.getId());
        fromHrmsNotifyConfigCreateRequest(hrmsNotifyConfig, request);
        return hrmsNotifyConfig;
    }

    public void fromHrmsNotifyConfigCreateRequest(HrmsNotifyConfig hrmsNotifyConfig, HrmsNotifyConfigSaveRequest request) {
        hrmsNotifyConfig.setEnabled(request.getEnabled());
        hrmsNotifyConfig.setNotifyChannel(request.getNotifyChannel());
        hrmsNotifyConfig.setPushPlusUrl(request.getPushPlusUrl());
        hrmsNotifyConfig.setPushPlusToken(request.getPushPlusToken());
        hrmsNotifyConfig.setDingTalkUrl(request.getDingTalkUrl());
        hrmsNotifyConfig.setHrmsUrl(request.getHrmsUrl());
        hrmsNotifyConfig.setHrmsToken(request.getHrmsToken());
    }
}
