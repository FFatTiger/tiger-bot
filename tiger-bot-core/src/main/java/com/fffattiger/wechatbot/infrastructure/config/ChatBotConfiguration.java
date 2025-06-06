package com.fffattiger.wechatbot.infrastructure.config;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.infrastructure.external.chatlog.ChatLogClient;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.RobotNameResponse;
import com.fffattiger.wechatbot.infrastructure.external.wchat.OperationTaskManager;
import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAutoWebSocketHttpClient;
import com.fffattiger.wechatbot.interfaces.event.MessageReceiveListener;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableConfigurationProperties(ChatBotProperties.class)
@Slf4j
public class ChatBotConfiguration {


    @Bean
    public ChatLogClient chatLogClient(RestClient.Builder restClientBuilder, ChatBotProperties properties) {
        return new ChatLogClient(restClientBuilder, properties);
    }


    @Bean
    public WxAuto wxAuto(ChatBotProperties chatBotProperties, OperationTaskManager operationTaskManager, ApplicationEventPublisher applicationEventPublisher, MessageReceiveListener messageReceiveListener) throws Exception {
        WxAutoWebSocketHttpClient wxAuto = new WxAutoWebSocketHttpClient(chatBotProperties, operationTaskManager, applicationEventPublisher);

        MessageHandler.Result<RobotNameResponse> robotName = wxAuto.getRobotName();
        if (robotName.success()) {
            chatBotProperties.setRobotName(robotName.data().robotName());
        } else {
            log.error("获取机器人名称失败 message: {}", robotName);
            throw new Exception("获取机器人名称失败 message: " + robotName);
        }

        return wxAuto;
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
