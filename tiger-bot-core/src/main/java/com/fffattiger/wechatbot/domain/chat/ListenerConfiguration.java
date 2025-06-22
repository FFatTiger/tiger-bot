package com.fffattiger.wechatbot.domain.chat;

import java.util.Arrays;

import jakarta.persistence.Embeddable;

/**
 * 监听器配置值对象
 * 包含聊天室的消息监听和处理配置
 */
@Embeddable
public record ListenerConfiguration(

    /**
     * 是否开启监听
     */
    boolean enable,

    /**
     * 是否开启@回复
     */
    boolean atReplyEnable,
    
    /**
     * 是否开启关键词回复
     */
    boolean keywordReplyEnable,
    
    /**
     * 关键词回复列表
     */
    String keywordReply
) {
    
    /**
     * 默认配置构造方法
     */
    public static ListenerConfiguration defaultConfig() {
        return new ListenerConfiguration(
            true,   // 默认开启监听
            true,   // 默认开启@回复
            false,  // 默认关闭关键词回复
            "" // 空关键词列表
        );
    }
    
    /**
     * 判断是否应该处理消息
     */
    public boolean shouldProcessMessage(String messageContent, String botName, boolean isGroupChat) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            return false;
        }

        // 群聊中需要@回复
        if (isGroupChat && atReplyEnable) {
            if (!messageContent.startsWith("@" + botName)) {
                return false;
            }
        }

        // 检查关键词回复
        if (keywordReplyEnable && keywordReply != null && !keywordReply.isEmpty()) {
            return Arrays.stream(keywordReply.split(","))
                .anyMatch(keyword -> messageContent.contains(keyword.trim()));
        }

        return true;
    }
    
    /**
     * 提取清理后的消息内容
     */
    public String extractCleanContent(String messageContent, String botName) {
        String realContent = messageContent;
        String atRobot = "@" + botName;
        int startIndex = messageContent.indexOf(atRobot) + atRobot.length();
        while (startIndex < messageContent.length() && Character.isWhitespace(messageContent.charAt(startIndex))) {
            startIndex++;
        }
        if (startIndex < messageContent.length()) {
            realContent = messageContent.substring(startIndex);
        }
        return realContent;
    }
    
    /**
     * 检查配置是否有效
     */
    public boolean isValidConfiguration() {
        // 如果开启关键词回复，必须至少有一个关键词
        if (keywordReplyEnable) {
            return keywordReply != null && !keywordReply.isEmpty();
        }
        return true;
    }
    
} 