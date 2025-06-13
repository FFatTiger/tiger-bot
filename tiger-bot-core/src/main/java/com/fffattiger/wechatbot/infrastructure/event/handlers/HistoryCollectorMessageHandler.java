package com.fffattiger.wechatbot.infrastructure.event.handlers;

import java.util.concurrent.ExecutorService;

import com.fffattiger.wechatbot.domain.chat.Chat;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.assembler.MessageAssembler;
import com.fffattiger.wechatbot.application.service.MessageApplicationService;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HistoryCollectorMessageHandler implements MessageHandler {



    @Resource
    private MessageApplicationService messageApplicationService;

    @Resource
    private MessageAssembler messageAssembler;

    @Resource
    private ExecutorService executorService;

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        WechatMessageSpecification.ChatSpecification.MessageSpecification messageSpecification = context.message();
        Chat chat = context.currentChat().chat();
        String chatName = chat.getName();
        String sender = messageSpecification.sender();
        Long timestamp = context.messageTimestamp();

        log.debug("收集聊天历史: 聊天={}, 发送者={}, 消息类型={}",
                chatName, sender, messageSpecification.type());

        try {
            executorService.execute(() -> messageApplicationService.save(messageAssembler.toMessage(messageSpecification, chat, timestamp)));
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
