package com.work.assistant.external.notify.handler.impl.pushplus;

import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.handler.NotifyHandler;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushPlusNotifyHandler implements NotifyHandler {
    @Override
    public NotifyType getNotifyType() {
        return null;
    }

    @Override
    public NotifyResponse notify(NotifyRequest request) {
        return null;
    }
}
