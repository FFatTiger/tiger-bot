package com.fffattiger.wechatbot.infrastructure.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;

import com.fffattiger.wechatbot.shared.properties.ChatModelConfig;

public class DynamicChatClient {

    private final Map<String, ChatClient> CHAT_CLIENT_MAP = new HashMap<>();

    
    public DynamicChatClient(List<ChatModelConfig> chatModelConfigs) {
    }

    public ChatClient chat(String provider) {
        return CHAT_CLIENT_MAP.get(provider);
    }

    public void addChatClient(String provider, ChatClient chatClient) {
        CHAT_CLIENT_MAP.put(provider, chatClient);
    }


    
}
