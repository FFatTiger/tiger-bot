package com.fffattiger.wechatbot.management.application.dto;

import java.util.List;

/**
 * 监听器配置DTO
 */
public record ListenerConfigurationDto(
    Long id,
    Long chatId,
    String chatName,
    boolean atReplyEnable,
    boolean keywordReplyEnable,
    boolean savePic,
    boolean saveVoice,
    boolean parseLinks,
    List<String> keywordReply
) {
    
    /**
     * 创建新监听器配置的构造方法
     */
    public static ListenerConfigurationDto forCreation(
            Long chatId,
            boolean atReplyEnable,
            boolean keywordReplyEnable,
            boolean savePic,
            boolean saveVoice,
            boolean parseLinks,
            List<String> keywordReply) {
        return new ListenerConfigurationDto(
            null, chatId, null, atReplyEnable, keywordReplyEnable,
            savePic, saveVoice, parseLinks, keywordReply
        );
    }
    
    /**
     * 检查配置是否有效
     */
    public boolean isValidConfiguration() {
        return chatId != null && chatId > 0;
    }
}
