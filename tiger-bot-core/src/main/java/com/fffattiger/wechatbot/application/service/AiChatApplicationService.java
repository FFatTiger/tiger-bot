package com.fffattiger.wechatbot.application.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.ChatClientBuilderFactory;
import com.fffattiger.wechatbot.domain.ai.repository.AiModelRepository;
import com.fffattiger.wechatbot.domain.ai.repository.AiProviderRepository;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;

@Service
public class AiChatApplicationService {

 
    @Resource
    private AiProviderRepository aiProviderRepository;

    @Resource
    private AiModelRepository aiModelRepository;

    @Resource
    private AiRoleRepository aiRoleRepository;

    @Resource
    private ChatRepository chatRepository;

    @Resource
    private ObjectProvider<RestClient.Builder> restClientBuilderProvider;

    public ChatClient.Builder builder(AiSpecification aiSpecification, Map<String, Object> params) {
        AiProvider aiProvider = aiProviderRepository.findById(aiSpecification.aiProviderId()).orElseThrow(() -> new RuntimeException("AI Provider not found"));
        AiModel aiModel = aiModelRepository.findById(aiSpecification.aiModelId()).orElseThrow(() -> new RuntimeException("AI Model not found"));
        AiRole aiRole = aiRoleRepository.findById(aiSpecification.aiRoleId()).orElseThrow(() -> new RuntimeException("AI Role not found"));

        return ChatClientBuilderFactory.builder(aiProvider, aiModel, aiRole, params, restClientBuilderProvider);
    }

    
    
}
