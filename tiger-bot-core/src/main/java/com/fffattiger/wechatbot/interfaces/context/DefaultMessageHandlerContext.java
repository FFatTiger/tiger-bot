package com.fffattiger.wechatbot.interfaces.context;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;

@SuppressWarnings("unchecked")
public class DefaultMessageHandlerContext implements MessageHandlerContext {

    private final TransmittableThreadLocal<Map<String, Object>> threadLocal = new TransmittableThreadLocal<>();


    public DefaultMessageHandlerContext(WxAuto wxAuto, Message message) {
        threadLocal.set(new ConcurrentHashMap<>());
        set("wxAuto", wxAuto);
        set("message", message);
    }

    public void set(String key, Object value) {
        threadLocal.get().put(key, value);
    }

    @Override
    public <T> T get(String key) {
        return (T) threadLocal.get().get(key);
    }

    @Override
    public Message getMessage() {
        return get("message");
    }

    @Override
    public String getRobotName() {
        return get("robotName");
    }

    @Override
    public void replyText(String text) {
        getWxAuto().sendText(getMessage().getChatName(), text);
    }

    public WxAuto getWxAuto() {
        return get("wxAuto");
    }

    @Override
    public void replyText(String chatName, String text) {
        getWxAuto().sendText(chatName, text);
    }

    @Override
    public void replyFile(File file) {
        getWxAuto().sendFileByUpload(getMessage().getChatName(), file);
    }

    public void clear() {
        threadLocal.remove();
    }

    @Override
    public void setMessage(Message message) {
        set("message", message);
    }

    @Override
    public void setRobotName(String robotName) {
        set("robotName", robotName);
    }

    @Override
    public void setIsGroupChat(boolean isGroupChat) {
        set("isGroupChat", isGroupChat);
    }

    @Override
    public boolean isGroupChat() {
        return get("isGroupChat");
    }

}