package com.work.assistant.external.notify.model;

import com.work.assistant.external.notify.enums.NotifyType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 通知请求
 */
@Data
public class NotifyRequest {

    /**
     * 通知类型
     */
    private NotifyType notifyType;


    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 接收人
     */
    private List<String> receivers;

    /**
     * 扩展参数
     */
    private Map<String, Object> ext;
}
