package com.fffattiger.wechatbot.application.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.repository.AiProviderRepository;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.infrastructure.ai.ChatClientBuilderFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiApplicationService {

    private final AiProviderRepository aiProviderRepository;

    private final AiRoleRepository aiRoleRepository;

    private final ObjectProvider<RestClient.Builder> restClientBuilderProvider;

    private final JdbcChatMemoryRepository chatMemoryRepository;

    public ChatClient.Builder builder(AiSpecification aiSpecification, Map<String, Object> params) {
        AiProvider aiProvider = aiProviderRepository.findById(aiSpecification.aiProviderId()).orElseThrow(() -> new RuntimeException("AiProvider not found"));
        AiModel aiModel = aiProvider.findModelById(aiSpecification.aiModelId()).orElseThrow(() -> new RuntimeException("AiModel not found"));
        AiRole aiRole = aiRoleRepository.findById(aiSpecification.aiRoleId()).orElseThrow(() -> new RuntimeException("AiRole not found"));

        return ChatClientBuilderFactory.builder(aiProvider, aiModel, aiRole, params, restClientBuilderProvider);
    }

    /**
     * 清除会话记忆
     * @param conversationId
     */
    public void clearMemory(String conversationId) {
        log.info("Clearing chat memory for conversationId: {}", conversationId);
        chatMemoryRepository.deleteByConversationId(conversationId);
    }

}
