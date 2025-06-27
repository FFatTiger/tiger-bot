package com.fffattiger.wechatbot.infrastructure.external.paste;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * CloudPaste服务配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "cloudpaste")
@Data
public class CloudPasteProperties {
    
    /**
     * CloudPaste服务的基础URL
     */
    private String baseBackendUrl;

    /**
     * CloudPaste服务的前端URL
     */
    private String baseFrontendUrl;
    
    /**
     * API访问令牌
     */
    private String apiToken;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;


} 