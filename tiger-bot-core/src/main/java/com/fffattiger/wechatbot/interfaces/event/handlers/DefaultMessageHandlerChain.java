package com.fffattiger.wechatbot.interfaces.event.handlers;

import java.util.Iterator;
import java.util.List;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class DefaultMessageHandlerChain implements MessageHandlerChain {

    private final List<MessageHandler> handlers;
    private Iterator<MessageHandler> iterator;

    public DefaultMessageHandlerChain(List<MessageHandler> handlers) {
        this.handlers = handlers;
        this.iterator = handlers.iterator();
    }

    @Override
    public boolean handle(MessageHandlerContext context) {
        if (iterator.hasNext()) {
            MessageHandler nextHandler = iterator.next();
            try {
                log.info("处理消息, handler: {}", nextHandler.getClass().getSimpleName());
                return nextHandler.handle(context, this);
            } catch (Exception e) {
                log.error("处理消息失败, handler: {}", nextHandler.getClass().getSimpleName(), e);
                return false;
            }
        }
        return false;
    }
    
}
