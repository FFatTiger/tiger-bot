package com.fffattiger.wechatbot.core.handler;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.core.ChatHistoryCollector;
import com.fffattiger.wechatbot.wxauto.MessageHandler;
import com.fffattiger.wechatbot.wxauto.MessageHandlerChain;
import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

import jakarta.annotation.Resource;

@Service
public class HistoryCollectorMessageHandler implements MessageHandler {

    @Resource
    private ChatHistoryCollector chatHistoryCollector;

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        Message message = context.message();
        String chatName = context.currentChat().getChatName();
        
        chatHistoryCollector.collect(chatName, message);
        
        return chain.handle(context);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
