package com.fffattiger.wechatbot.infrastructure.event.handlers;

import java.util.concurrent.ExecutorService;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.assembler.MessageAssembler;
import com.fffattiger.wechatbot.application.service.MessageApplicationService;
import com.fffattiger.wechatbot.api.Message;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HistoryCollectorMessageHandler implements MessageHandlerExtension {



    @Resource
    private MessageApplicationService messageApplicationService;

    @Resource
    private MessageAssembler messageAssembler;

    @Resource
    private ExecutorService executorService;

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String chatName = message.chatName();
        String sender = message.senderName();
        Long timestamp = message.timestamp();

        log.debug("收集聊天历史: 聊天={}, 发送者={}, 消息类型={}",
                chatName, sender, message.type());

        try {
            // executorService.execute(() -> messageApplicationService.save(messageAssembler.toMessage(message, chatName, sender, timestamp)));
            log.debug("聊天历史收集成功: 聊天={}, 发送者={}", chatName, sender);
        } catch (Exception e) {
            log.error("聊天历史收集失败: 聊天={}, 发送者={}, 错误信息={}",
                    chatName, sender, e.getMessage(), e);
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
