package com.fffattiger.wechatbot.interfaces.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler.WechatMessageSpecification.ChatSpecification.MessageSpecification;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

@SuppressWarnings("unchecked")
public class DefaultMessageHandlerContext implements MessageHandlerContext {

    private final TransmittableThreadLocal<Map<String, Object>> threadLocal = new TransmittableThreadLocal<>();

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
    public MessageSpecification message() {
        return get("message");
    }

    @Override
    public void setMessage(MessageHandler.WechatMessageSpecification.ChatSpecification.MessageSpecification messageSpecification) {
        set("message", messageSpecification);
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
    public void setCurrentChat(MessageProcessingData currentChat) {
        set("currentChat", currentChat);
    }

    @Override
    public MessageProcessingData currentChat() {
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