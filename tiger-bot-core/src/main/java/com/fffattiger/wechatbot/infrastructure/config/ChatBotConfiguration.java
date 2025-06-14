package com.fffattiger.wechatbot.infrastructure.config;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;
import com.fffattiger.wechatbot.shared.properties.DatabaseInitProperties;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableConfigurationProperties({ChatBotProperties.class, DatabaseInitProperties.class})
@Slf4j
public class ChatBotConfiguration {

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
