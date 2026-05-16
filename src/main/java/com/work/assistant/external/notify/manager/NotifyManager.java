package com.work.assistant.external.notify.manager;

import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.handler.NotifyHandler;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知管理器
 */
@Slf4j
@Component
public class NotifyManager {

    /**
     * 通知处理器映射
     */
    private final Map<NotifyType, NotifyHandler> handlerMap;

    public NotifyManager(List<NotifyHandler> handlers) {

        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        NotifyHandler::getNotifyType,
                        Function.identity()
                ));

        log.info("notify handler init success, handlers={}",
                handlerMap.keySet());
    }

    /**
     * 统一通知入口
     */
    public NotifyResponse notify(NotifyRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "notify request can not be null"
            );
        }

        NotifyType notifyType =
                request.getNotifyType();

        if (notifyType == null) {
            throw new IllegalArgumentException(
                    "notify type can not be null"
            );
        }

        NotifyHandler handler =
                handlerMap.get(notifyType);

        if (handler == null) {

            throw new IllegalArgumentException(
                    "unsupported notify type: "
                            + notifyType
            );
        }

        return handler.notify(request);
    }
}
