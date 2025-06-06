package com.fffattiger.wechatbot.interfaces.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

@SuppressWarnings("unchecked")
public class DefaultMessageHandlerContext implements MessageHandlerContext {

    private final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public DefaultMessageHandlerContext() {
        threadLocal.set(new ConcurrentHashMap<>());
    }

    public void set(String key, Object value) {
        threadLocal.get().put(key, value);
    }

    @Override
    public <T> T get(String key) {
        return (T) threadLocal.get().get(key);
    }

    @Override
    public Message message() {
        return get("message");
    }

    @Override
    public void setMessage(Message message) {
        set("message", message);
    }

    @Override
    public void setWxAuto(WxAuto wxAuto) {
        set("wxAuto", wxAuto);
    }

    @Override
    public WxAuto wx() {
        return get("wxAuto");
    }

    @Override
    public void setCurrentChat(ListenerAggregate currentChat) {
        set("currentChat", currentChat);
    }

    @Override
    public ListenerAggregate currentChat() {
        return get("currentChat");
    }

    
    public void clear() {
        threadLocal.remove();
    }

    @Override
    public ChatBotProperties chatBotProperties() {
        return get("chatBotProperties");
    }

    @Override
    public void setChatBotProperties(ChatBotProperties chatBotProperties) {
        set("chatBotProperties", chatBotProperties);
    }

    @Override
    public String cleanContent() {
        return get("cleanContent");
    }

    @Override
    public void setCleanContent(String cleanContent) {
        set("cleanContent", cleanContent);
    }

    @Override
    public Long messageTimestamp() {
        return get("messageTimestamp");
    }

    @Override
    public void setMessageTimestamp(Long messageTimestamp) {
        set("messageTimestamp", messageTimestamp);
    }


}