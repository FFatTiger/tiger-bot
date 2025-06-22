package com.fffattiger.wechatbot.domain.chat;

/**
 * 消息处理状态枚举
 */
public enum MessageStatus {
    /**
     * 待处理
     */
    PENDING,
    
    /**
     * 已处理
     */
    PROCESSED,
    
    /**
     * 已忽略
     */
    IGNORED,
    
    /**
     * 处理失败
     */
    FAILED
} 