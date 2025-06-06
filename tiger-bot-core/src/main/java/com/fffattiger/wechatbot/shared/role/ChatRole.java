package com.fffattiger.wechatbot.shared.role;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;

import lombok.Data;

public class ChatRole {

    /**
     * 角色名称
     */
    private String roleName;


    /**
     * 角色提示词
     */
    private Prompt prompt;


    /**
     * 角色的记忆
     */
    private ChatMemory chatMemory;

    /**
     * brain
     */
    private ChatClient brain;

}
