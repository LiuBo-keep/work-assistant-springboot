package com.work.assistant.external.notify.model;

import lombok.Builder;
import lombok.Data;

/**
 * 通知响应
 */
@Data
@Builder
public class NotifyResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;
}