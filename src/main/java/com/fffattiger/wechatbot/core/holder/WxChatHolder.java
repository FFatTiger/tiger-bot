package com.fffattiger.wechatbot.core.holder;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.core.WxChat;

@Component
public class WxChatHolder {

    public static Map<String, WxChat> WX_CHAT_MAP = new HashMap<>();
    

    public static WxChat getWxChat(String chatName){
        return WX_CHAT_MAP.get(chatName);
    }

    public static void registerWxChat(WxChat wxChat){
        WX_CHAT_MAP.put(wxChat.getChatName(), wxChat);
    }
}