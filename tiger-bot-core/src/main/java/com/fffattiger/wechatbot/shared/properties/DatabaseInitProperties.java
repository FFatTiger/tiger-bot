package com.fffattiger.wechatbot.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 数据库初始化配置属性
 */
@ConfigurationProperties(prefix = "chatbot.database.init")
@Data
public class DatabaseInitProperties {
    
    /**
     * 是否启用数据库初始化检查
     */
    private boolean enabled = true;
    
    /**
     * 是否强制重新创建表（慎用）
     */
    private boolean forceRecreate = false;
    
    /**
     * 建表脚本路径
     */
    private String schemaLocation = "classpath:scripts/schema-postgresql.sql";
    
    /**
     * 初始化数据脚本路径
     */
    private String dataLocation = "classpath:scripts/data.sql";
}
