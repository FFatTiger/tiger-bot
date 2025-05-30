package com.fffattiger.wechatbot.config;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fffattiger.wechatbot.core.AsyncChatHistoryCollector;
import com.fffattiger.wechatbot.core.ChatHistoryCollector;
import com.fffattiger.wechatbot.core.WxChat;
import com.fffattiger.wechatbot.core.holder.WxChatHolder;
import com.fffattiger.wechatbot.wxauto.MessageHandler;
import com.fffattiger.wechatbot.wxauto.OperationTaskManager;
import com.fffattiger.wechatbot.wxauto.WxAuto;
import com.fffattiger.wechatbot.wxauto.WxAutoWebSocketHttpClient;
import com.fffattiger.wechatbot.wxauto.MessageHandler.RobotNameResponse;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableConfigurationProperties(ChatBotProperties.class)
@Slf4j
public class ChatBotConfiguration {

    @Bean
    public OperationTaskManager operationTaskManager() {
        return new OperationTaskManager();
    }

    @Bean
    public WxAuto wxAuto(List<MessageHandler> messageHandlers, ChatBotProperties chatBotProperties, WebClient.Builder webClientBuilder, OperationTaskManager operationTaskManager) throws Exception {
        WxAutoWebSocketHttpClient wxAuto = new WxAutoWebSocketHttpClient(messageHandlers, chatBotProperties, operationTaskManager);

        for (WxChatConfig listener : chatBotProperties.getListeners()) {
            WxChat wxChat = new WxChat(listener, null);
            WxChatHolder.registerWxChat(wxChat);
            wxAuto.addListenChat(listener.getWxInfo().getChatName(), true, true, true);
        }

        com.fffattiger.wechatbot.wxauto.MessageHandler.ApiResponse<RobotNameResponse> robotName = wxAuto.getRobotName();
        if (robotName.success()) {
            chatBotProperties.setRobotName(robotName.data().robotName());
        } else {
            log.error("获取机器人名称失败 message: {}", robotName);
            throw new Exception("获取机器人名称失败 message: " + robotName);
        }

        return wxAuto;
    }

    @Bean
    @ConditionalOnMissingBean(ChatHistoryCollector.class)
    public ChatHistoryCollector chatHistoryCollector() {
        return new AsyncChatHistoryCollector();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }

}
