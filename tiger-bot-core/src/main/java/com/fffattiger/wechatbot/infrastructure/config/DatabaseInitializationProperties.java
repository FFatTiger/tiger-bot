package com.fffattiger.wechatbot.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;

/**
 * 数据库初始化配置属性
 */
@ConfigurationProperties(prefix = "chatbot.database")
@Data
public class DatabaseInitializationProperties {
    
    /**
     * 数据库初始化配置
     */
    @NestedConfigurationProperty
    private Init init = new Init();
    
    @Data
    public static class Init {
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
}
