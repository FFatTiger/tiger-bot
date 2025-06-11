package com.fffattiger.wechatbot.infrastructure.config;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.infrastructure.event.MessageReceiveListener;
import com.fffattiger.wechatbot.infrastructure.startup.DatabaseStartupInitializer;
import com.fffattiger.wechatbot.infrastructure.external.chatlog.ChatLogClient;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.RobotNameResponse;
import com.fffattiger.wechatbot.infrastructure.external.wchat.OperationTaskManager;
import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAutoWebSocketHttpClient;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;
import com.fffattiger.wechatbot.shared.properties.DatabaseInitProperties;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableConfigurationProperties({ChatBotProperties.class, DatabaseInitProperties.class})
@Slf4j
public class ChatBotConfiguration {


    @Bean
    public ChatLogClient chatLogClient(RestClient.Builder restClientBuilder, ChatBotProperties properties) {
        return new ChatLogClient(restClientBuilder, properties);
    }

    @Bean
    public DatabaseStartupInitializer databaseStartupInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader, DatabaseInitProperties properties) {
        return new DatabaseStartupInitializer(jdbcTemplate, resourceLoader, properties);
    }


    @Bean
    public WxAuto wxAuto(ChatBotProperties chatBotProperties, OperationTaskManager operationTaskManager, ApplicationEventPublisher applicationEventPublisher, MessageReceiveListener messageReceiveListener, DatabaseStartupInitializer databaseStartupInitializer) throws Exception {
        databaseStartupInitializer.initDatabase();  
        
        log.info("初始化微信自动化客户端: 网关地址={}", chatBotProperties.getWxAutoGatewayHttpUrl());

        WxAutoWebSocketHttpClient wxAuto = new WxAutoWebSocketHttpClient(chatBotProperties, operationTaskManager, applicationEventPublisher);

        log.info("获取机器人名称...");
        MessageHandler.Result<RobotNameResponse> robotName = wxAuto.getRobotName();
        if (robotName.success()) {
            String botName = robotName.data().robotName();
            chatBotProperties.setRobotName(botName);
            log.info("机器人名称获取成功: {}", botName);
        } else {
            log.error("获取机器人名称失败: {}", robotName.message());
            throw new Exception("获取机器人名称失败 message: " + robotName);
        }

        log.info("微信自动化客户端初始化完成");
        return wxAuto;
    }

    @Bean
    public ExecutorService executorService() {
        log.info("创建消息处理线程池: 线程数=10");
        return Executors.newFixedThreadPool(10);
    }
}
