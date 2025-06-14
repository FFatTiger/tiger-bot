package com.fffattiger.wechatbot.shared.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "chatbot")
@Data
public class ChatBotProperties {

    /**
     * apiKey
     */
    private String apiKey;

    /**
     * 命令前缀
     */
    private String commandPrefix = "/";

    /**
     * 插件目录
     */
    private String pluginDir = "plugins";

    /**
     * 机器人名称
     */
    private String robotName;


    /**
     * 聊天记录API地址
     */
    private String chatLogApiUrl;


    /**
     * 微信网关基础URL
     */
    private String wxAutoGatewayHttpUrl;


    /**
     * 微信网关基础URL
     */
    private String wxAutoGatewayWsUrl;


    /**
     * SSE连接超时时间
     */
    private Duration sseTimeout = Duration.ofSeconds(30);
    
    /**
     * HTTP请求超时时间
     */
    private Duration httpTimeout = Duration.ofSeconds(30);
    
    /**
     * SSE重连间隔
     */
    private Duration reconnectInterval = Duration.ofSeconds(5);
    
    /**
     * 最大重连次数
     */
    private int maxReconnectAttempts = 10;
    
    /**
     * 操作队列大小
     */
    private int operationQueueSize = 1000;
    
    /**
     * 是否启用健康检查
     */
    private boolean healthCheckEnabled = true;
    
    /**
     * 健康检查间隔
     */
    private Duration healthCheckInterval = Duration.ofMinutes(1);

    /**
     * 临时文件目录
     */
    private String tempFileDir = "output";

    /**
     * 聊天记忆目录
     */
    private String chatMemoryDir = "chatMemory";

    /**
     * 聊天记忆文件最大数量
     */
    private int chatMemoryFileMaxCount = 100;
}