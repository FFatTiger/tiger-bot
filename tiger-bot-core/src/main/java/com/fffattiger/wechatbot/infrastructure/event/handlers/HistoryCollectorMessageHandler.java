package com.fffattiger.wechatbot.infrastructure.event.handlers;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.ai.service.ChatHistoryCollector;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HistoryCollectorMessageHandler implements MessageHandler {

    @Resource
    private ChatHistoryCollector chatHistoryCollector;

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        Message message = context.message();
        String chatName = context.currentChat().chat().getName();
        String sender = message.sender();

        log.debug("收集聊天历史: 聊天={}, 发送者={}, 消息类型={}",
                chatName, sender, message.type());

        try {
            chatHistoryCollector.collect(chatName, message, context.messageTimestamp());
            log.debug("聊天历史收集成功: 聊天={}, 发送者={}", chatName, sender);
        } catch (Exception e) {
            log.error("聊天历史收集失败: 聊天={}, 发送者={}, 错误信息={}",
                    chatName, sender, e.getMessage(), e);
        }

        return chain.handle(context);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
