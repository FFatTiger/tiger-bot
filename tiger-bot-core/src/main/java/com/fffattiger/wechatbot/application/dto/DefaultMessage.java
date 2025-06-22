package com.fffattiger.wechatbot.application.dto;

import java.time.ZoneOffset;

import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.domain.chat.event.MessageReceivedEvent;

public class DefaultMessage implements Message {

    private final Long id;
    private final Long chatId;
    private final String chatName;
    private final String type;
    private final String attr;
    private final String hash;
    private final String rawContent;
    private String cleanContent;
    private final String senderId;
    private final String senderName;
    private final Long timestamp;

    public DefaultMessage(MessageReceivedEvent event) {
        this.id = event.getMessage().getId();
        this.chatId = event.getMessage().getChatId();
        this.chatName = event.getChat().getName();
        this.type = event.getMessage().getType().getValue();
        this.attr = event.getMessage().getAttr().getValue();
        this.hash = event.getMessage().getHash();
        this.rawContent = event.getMessage().getContent();
        this.cleanContent = event.getMessage().getContent();
        this.senderId = event.getMessage().getSender();
        this.senderName = event.getMessage().getSender();
        this.timestamp = event.getMessage().getTime().toEpochSecond(ZoneOffset.of("+8"));
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getChatId() {
        return chatId;
    }

    @Override
    public String getChatName() {
        return chatName;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getAttr() {
        return attr;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public String getRawContent() {
        return rawContent;
    }

    @Override
    public String getCleanContent() {
        return cleanContent;
    }

    @Override
    public String getSenderId() {
        return senderId;
    }

    @Override
    public String getSenderName() {
        return senderName;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    public void updateCleanContent(String cleanContent) {
        this.cleanContent = cleanContent;
    }
}