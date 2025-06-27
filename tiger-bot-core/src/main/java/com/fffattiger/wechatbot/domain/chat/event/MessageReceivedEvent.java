package com.fffattiger.wechatbot.domain.chat.event;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.ChatMessage;
import com.fffattiger.wechatbot.domain.common.DomainEvent;

public class MessageReceivedEvent extends DomainEvent {

    private final Chat chat;

    private final ChatMessage message;

    public MessageReceivedEvent(Chat chat, ChatMessage message) {
        this.chat = chat;
        this.message = message;
    }

    public Chat getChat() {
        return chat;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
