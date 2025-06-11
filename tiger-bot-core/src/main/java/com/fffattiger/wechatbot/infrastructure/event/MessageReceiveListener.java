package com.fffattiger.wechatbot.infrastructure.event;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.infrastructure.event.handlers.DefaultMessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages;
import com.fffattiger.wechatbot.interfaces.context.DefaultMessageHandlerContext;
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
        log.info("开始关闭消息接收监听器");

        messageProcessorPool.shutdown();
        log.info("消息处理线程池已发送关闭信号");

        try {
            if (!messageProcessorPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                log.warn("消息处理线程池未在5秒内正常关闭，强制关闭");
                messageProcessorPool.shutdownNow();
            } else {
                log.info("消息处理线程池已正常关闭");
            }
        } catch (InterruptedException e) {
            log.error("等待消息处理线程池关闭时被中断", e);
            messageProcessorPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("消息接收监听器关闭完成");
    }

    @Override
    public void onApplicationEvent(@NonNull MessageReceiveEvent event) {
        log.info("接收到消息事件，时间戳: {}, 聊天数量: {}",
                event.getMessage().timestamp(), event.getMessage().data().size());

        // 处理微信消息
        for (BatchedSanitizedWechatMessages.Chat chat : event.getMessage().data()) {
            log.info("处理聊天: {}, 消息数量: {}", chat.chatName(), chat.messages().size());

            MessageProcessingData messageProcessingData = listenerApplicationService.getMessageProcessingData(chat.chatName());
            if (messageProcessingData == null) {
                log.warn("未监听该对象: {}", chat.chatName());
                continue;
            }

            log.info("找到监听配置: 聊天={}, 监听器ID={}",
                    chat.chatName(), messageProcessingData.listener().getId());

            for (BatchedSanitizedWechatMessages.Chat.Message msg : chat.messages()) {
                log.info("提交消息处理任务: 聊天={}, 发送者={}, 消息类型={}, 内容长度={}",
                        chat.chatName(), msg.sender(), msg.type(),
                        msg.content() != null ? msg.content().length() : 0);

                messageProcessorPool.submit(() -> {
                    try {
                        log.debug("开始处理消息: 聊天={}, 发送者={}, 消息ID={}",
                                chat.chatName(), msg.sender(), msg.id());

                        DefaultMessageHandlerContext context = new DefaultMessageHandlerContext();
                        context.setMessage(msg);
                        context.setWxAuto(event.getWxAuto());
                        context.setCurrentChat(messageProcessingData);
                        context.setChatBotProperties(chatBotProperties);
                        context.setMessageTimestamp(event.getMessage().timestamp());

                        boolean handled = new DefaultMessageHandlerChain(messageHandlers).handle(context);

                        log.info("消息处理完成: 聊天={}, 发送者={}, 处理结果={}",
                                chat.chatName(), msg.sender(), handled ? "已处理" : "未处理");

                    } catch (Exception e) {
                        log.error("消息处理异常: 聊天={}, 发送者={}, 错误信息={}",
                                chat.chatName(), msg.sender(), e.getMessage(), e);
                    }
                });
            }
        }

        log.debug("消息事件处理完成，已提交所有处理任务到线程池");
    }
}
