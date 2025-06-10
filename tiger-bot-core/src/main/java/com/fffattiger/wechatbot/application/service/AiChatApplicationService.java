package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

    /**
     * 获取所有AI角色
     */
    public List<AiRole> getAllRoles() {
        List<AiRole> roles = new ArrayList<>();
        aiRoleRepository.findAll().forEach(roles::add);
        return roles;
    }

    /**
     * 根据名称查找AI角色
     */
    public Optional<AiRole> getRoleByName(String roleName) {
        return getAllRoles().stream()
                .filter(role -> role.name().equals(roleName))
                .findFirst();
    }

    /**
     * 切换聊天的AI角色
     */
    public Chat changeRole(String chatName, String roleName) {
        // 查找聊天对象
        Chat chat = chatRepository.findByName(chatName)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatName));

        // 查找新角色
        AiRole newRole = getRoleByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // 创建新的AI配置，保持原有的提供商和模型配置
        AiSpecification currentSpec = chat.getAiSpecification();
        AiSpecification newSpec = new AiSpecification(
                currentSpec.aiProviderId(),
                currentSpec.aiModelId(),
                newRole.id()
        );

        // 更新聊天的AI配置
        chat.updateAiConfiguration(newSpec);

        Chat savedChat = chatRepository.save(chat);
        log.info("切换聊天 {} 的AI角色为: {}", chatName, roleName);
        return savedChat;
    }
}
