package com.fffattiger.wechatbot.core;

import org.springframework.ai.chat.client.ChatClient;

import com.fffattiger.wechatbot.config.WxChatConfig;

public class WxChat {

    private WxChatConfig wxChatConfig;

    private ChatClient chatClient;
    
    public WxChat(WxChatConfig wxChatConfig, ChatClient chatClient){
        this.wxChatConfig = wxChatConfig;
        this.chatClient = chatClient;
    }
    

    
    public String getChatName(){
        return wxChatConfig.getWxInfo().getChatName();
    }

    public boolean isGroup(){
        return wxChatConfig.getWxInfo().isGroupFlg();
    }

    public boolean isAiChatEnable(){
        return wxChatConfig.getAiChatInfo().isAiChatEnable();
    }

    public ChatClient getChatClient() {
        return chatClient;
    }

    public WxChatConfig getWxChatConfig() {
        return wxChatConfig;
    }
    
}
