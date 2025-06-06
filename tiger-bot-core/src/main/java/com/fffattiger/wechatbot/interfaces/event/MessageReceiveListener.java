package com.fffattiger.wechatbot.interfaces.event;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages;
import com.fffattiger.wechatbot.interfaces.context.DefaultMessageHandlerContext;
import com.fffattiger.wechatbot.interfaces.event.handlers.DefaultMessageHandlerChain;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageReceiveListener implements ApplicationListener<MessageReceiveEvent> {

    @Resource
    private ExecutorService messageProcessorPool;

    @Resource
    private ChatBotProperties chatBotProperties;

    @Resource
    private List<MessageHandler> messageHandlers;

    @Resource
    private ListenerApplicationService listenerApplicationService;


    @PreDestroy
    public void shutdown() {
        log.info("Shutting down MessageReceiveListener...");

        messageProcessorPool.shutdown();

        try {
            if (!messageProcessorPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                messageProcessorPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessorPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("MessageReceiveListener shutdown complete");
    }

    @Override
    public void onApplicationEvent(@NonNull MessageReceiveEvent event) {
        // 处理微信消息
        for (BatchedSanitizedWechatMessages.Chat chat : event.getMessage().data()) {
            ListenerAggregate listenerAggregate = listenerApplicationService.getListenerAggregate(chat.chatName());
            if (listenerAggregate == null) {
                log.warn("未监听该对象: {}", chat.chatName());
                continue;
            }

            for (BatchedSanitizedWechatMessages.Chat.Message msg : chat.messages()) {
                messageProcessorPool.submit(() -> {
                    DefaultMessageHandlerContext context = new DefaultMessageHandlerContext();
                    context.setMessage(msg);
                    context.setWxAuto(event.getWxAuto());
                    context.setCurrentChat(listenerAggregate);
                    context.setChatBotProperties(chatBotProperties);
                    context.setMessageTimestamp(event.getMessage().timestamp());
                    new DefaultMessageHandlerChain(messageHandlers).handle(context);
                });
            }
        }
    }
}
