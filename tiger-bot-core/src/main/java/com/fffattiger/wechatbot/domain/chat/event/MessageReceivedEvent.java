package com.fffattiger.wechatbot.domain.chat.event;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.Message;
import com.fffattiger.wechatbot.domain.common.DomainEvent;

public class MessageReceivedEvent extends DomainEvent {

    private final Chat chat;

    private final Message message;

    public MessageReceivedEvent(Chat chat, Message message) {
        this.chat = chat;
        this.message = message;
    }

    public Chat getChat() {
        return chat;
    }

    public Message getMessage() {
        return message;
    }
}
