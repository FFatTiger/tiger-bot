package com.fffattiger.wechatbot.application.handler;

import java.util.List;
import java.util.stream.Collectors;

import com.fffattiger.wechatbot.api.handler.MessageHandler;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageHandlerChain {

    private final List<MessageHandler> handlers;

    public DefaultMessageHandlerChain(List<MessageHandler> handlers) {
        this.handlers = handlers.stream().sorted().collect(Collectors.toList());
    }

    public boolean handle(MessageHandlerContext context) { 

        for (MessageHandler handler : handlers) {
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
