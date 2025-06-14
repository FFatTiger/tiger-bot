package com.fffattiger.wechatbot.infrastructure.event.handlers;

import java.util.List;
import java.util.stream.Collectors;

import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageHandlerChain {

    private final List<MessageHandlerExtension> handlers;

    public DefaultMessageHandlerChain(List<MessageHandlerExtension> handlers) {
        this.handlers = handlers.stream().sorted().collect(Collectors.toList());
    }

    public boolean handle(MessageHandlerContext context) { 

        for (MessageHandlerExtension handler : handlers) {
            try {
                if (handler.handle(context)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("消息处理器 [{}] 执行异常", handler.getClass().getSimpleName(), e);
                return false;
            }
        }
        return false;
    }

}
