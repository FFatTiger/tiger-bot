package com.fffattiger.wechatbot.interfaces.event.handlers;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.ai.service.ChatHistoryCollector;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

import jakarta.annotation.Resource;

@Service
public class HistoryCollectorMessageHandler implements MessageHandler {

    @Resource
    private ChatHistoryCollector chatHistoryCollector;

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        Message message = context.message();
        String chatName = context.currentChat().chat().name();
        
        chatHistoryCollector.collect(chatName, message, context.messageTimestamp());
        
        return chain.handle(context);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
