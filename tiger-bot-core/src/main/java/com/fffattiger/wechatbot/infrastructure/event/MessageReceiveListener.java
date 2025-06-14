package com.fffattiger.wechatbot.infrastructure.event;

import java.util.concurrent.ExecutorService;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.api.Message;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.infrastructure.event.handlers.DefaultMessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto.WechatMessageSpecification;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;
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
    private ListenerApplicationService listenerApplicationService;

    @Resource
    private PluginHolder pluginHolder;

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
        for (WechatMessageSpecification.ChatSpecification chatSpecification : event.getMessage().data()) {
            log.info("处理聊天: {}, 消息数量: {}", chatSpecification.chatName(),
                    chatSpecification.messageSpecifications().size());

            MessageProcessingData messageProcessingData = listenerApplicationService
                    .getMessageProcessingData(chatSpecification.chatName());
            if (messageProcessingData == null) {
                log.warn("未监听该对象: {}", chatSpecification.chatName());
                continue;
            }

            for (WechatMessageSpecification.ChatSpecification.MessageSpecification msg : chatSpecification
                    .messageSpecifications()) {
                messageProcessorPool.submit(() -> {
                    try {
                        log.debug("开始处理消息: 聊天={}, 发送者={}, 消息ID={}",
                                chatSpecification.chatName(), msg.sender(), msg.id());

                        Message message = new Message(msg.id(), msg.type().getValue(), msg.content(), msg.content(),
                                msg.sender(), msg.senderRemark(), messageProcessingData.chat().getId(),
                                chatSpecification.chatName(), event.getMessage().timestamp());
                        MessageHandlerContext context = new DefaultMessageHandlerContext(event.getWxAuto());
                        context.setMessage(message);
                        context.setRobotName(chatBotProperties.getRobotName());
                        context.setIsGroupChat(messageProcessingData.chat().isGroupChat());
                        context.set("tempFileDir", chatBotProperties.getTempFileDir());
                        context.set("aiSpecification", messageProcessingData.chat().getAiSpecification());
                        context.set("listener", messageProcessingData.listener());
                        context.set("commandAuths", messageProcessingData.commandAuths());



                        new DefaultMessageHandlerChain(pluginHolder.getAllExtensions())
                                .handle(context);
                        context.clear();
                    } catch (Exception e) {
                        log.error("消息处理异常: 聊天={}, 发送者={}, 错误信息={}",
                                chatSpecification.chatName(), msg.sender(), e.getMessage(), e);
                    }
                });
            }
        }

        log.debug("消息事件处理完成，已提交所有处理任务到线程池");
    }
}
