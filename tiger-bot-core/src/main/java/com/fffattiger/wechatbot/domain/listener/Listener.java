package com.fffattiger.wechatbot.domain.listener;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("listeners")
public class Listener {
    @Id
    private Long id;

    /**
     * 监听的对象id
     */
    private Long chatId;

    /**
     * 是否开启@回复
     */
    private boolean atReplyEnable;

    /**
     * 是否开启关键词回复
     */
    private boolean keywordReplyEnable;

    /**
     * 是否保存图片
     */
    private boolean savePic;

    /**
     * 是否保存语音
     */
    private boolean saveVoice;

    /**
     * 是否解析链接
     */
    private boolean parseLinks;

    /**
     * 关键词回复
     */
    private List<String> keywordReply;

    // 构造函数
    public Listener() {}

    public Listener(Long id, Long chatId, boolean atReplyEnable, boolean keywordReplyEnable,
                   boolean savePic, boolean saveVoice, boolean parseLinks, List<String> keywordReply) {
        this.id = id;
        this.chatId = chatId;
        this.atReplyEnable = atReplyEnable;
        this.keywordReplyEnable = keywordReplyEnable;
        this.savePic = savePic;
        this.saveVoice = saveVoice;
        this.parseLinks = parseLinks;
        this.keywordReply = keywordReply;
    }

    // 业务方法
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
            return keywordReply.stream()
                .anyMatch(keyword -> messageContent.contains(keyword));
        }

        return true;
    }

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

        messageContent = realContent;
        return messageContent;
    }

    public boolean isValidConfiguration() {
        return chatId != null;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public boolean isAtReplyEnable() {
        return atReplyEnable;
    }

    public boolean isKeywordReplyEnable() {
        return keywordReplyEnable;
    }

    public boolean isSavePic() {
        return savePic;
    }

    public boolean isSaveVoice() {
        return saveVoice;
    }

    public boolean isParseLinks() {
        return parseLinks;
    }

    public List<String> getKeywordReply() {
        return keywordReply;
    }

    // Setters (仅用于框架)
    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setAtReplyEnable(boolean atReplyEnable) {
        this.atReplyEnable = atReplyEnable;
    }

    public void setKeywordReplyEnable(boolean keywordReplyEnable) {
        this.keywordReplyEnable = keywordReplyEnable;
    }

    public void setSavePic(boolean savePic) {
        this.savePic = savePic;
    }

    public void setSaveVoice(boolean saveVoice) {
        this.saveVoice = saveVoice;
    }

    public void setParseLinks(boolean parseLinks) {
        this.parseLinks = parseLinks;
    }

    public void setKeywordReply(List<String> keywordReply) {
        this.keywordReply = keywordReply;
    }
}
