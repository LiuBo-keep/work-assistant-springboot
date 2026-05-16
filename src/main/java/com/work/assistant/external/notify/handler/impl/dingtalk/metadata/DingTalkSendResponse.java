package com.work.assistant.external.notify.handler.impl.dingtalk.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DingTalkSendResponse {

    @JsonProperty("errcode")
    private int errCode;

    @JsonProperty("errmsg")
    private String errMsg;

}