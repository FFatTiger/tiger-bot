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
import com.fffattiger.wechatbot.domain.ai.repository.AiModelRepository;
import com.fffattiger.wechatbot.domain.ai.repository.AiProviderRepository;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.infrastructure.ai.ChatClientBuilderFactory;

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
        log.debug("构建AI聊天客户端: 提供商ID={}, 模型ID={}, 角色ID={}",
                aiSpecification.aiProviderId(), aiSpecification.aiModelId(), aiSpecification.aiRoleId());

        try {
            AiProvider aiProvider = aiProviderRepository.findById(aiSpecification.aiProviderId())
                    .orElseThrow(() -> new RuntimeException("AI Provider not found: " + aiSpecification.aiProviderId()));
            AiModel aiModel = aiModelRepository.findById(aiSpecification.aiModelId())
                    .orElseThrow(() -> new RuntimeException("AI Model not found: " + aiSpecification.aiModelId()));
            AiRole aiRole = aiRoleRepository.findById(aiSpecification.aiRoleId())
                    .orElseThrow(() -> new RuntimeException("AI Role not found: " + aiSpecification.aiRoleId()));

            log.debug("AI配置加载成功: 提供商={}, 模型={}, 角色={}",
                    aiProvider.providerName(), aiModel.modelName(), aiRole.name());

            return ChatClientBuilderFactory.builder(aiProvider, aiModel, aiRole, params, restClientBuilderProvider);

        } catch (Exception e) {
            log.error("构建AI聊天客户端失败: 提供商ID={}, 模型ID={}, 角色ID={}, 错误信息={}",
                    aiSpecification.aiProviderId(), aiSpecification.aiModelId(),
                    aiSpecification.aiRoleId(), e.getMessage(), e);
            throw e;
        }
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
        log.info("切换AI角色: 聊天={}, 目标角色={}", chatName, roleName);

        try {
            // 查找聊天对象
            Chat chat = chatRepository.findByName(chatName)
                    .orElseThrow(() -> new RuntimeException("Chat not found: " + chatName));
            log.debug("找到聊天对象: ID={}, 当前AI配置={}", chat.getId(),
                    chat.getAiSpecification() != null ? "已配置" : "未配置");

            // 查找新角色
            AiRole newRole = getRoleByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            log.debug("找到目标角色: ID={}, 名称={}", newRole.id(), newRole.name());

            // 创建新的AI配置，保持原有的提供商和模型配置
            AiSpecification currentSpec = chat.getAiSpecification();
            if (currentSpec == null) {
                log.error("聊天对象缺少AI配置: 聊天={}", chatName);
                throw new RuntimeException("Chat has no AI configuration: " + chatName);
            }

            AiSpecification newSpec = new AiSpecification(
                    currentSpec.aiProviderId(),
                    currentSpec.aiModelId(),
                    newRole.id()
            );
            log.debug("创建新AI配置: 提供商ID={}, 模型ID={}, 角色ID={}",
                    newSpec.aiProviderId(), newSpec.aiModelId(), newSpec.aiRoleId());

            // 更新聊天的AI配置
            chat.updateAiConfiguration(newSpec);
            Chat savedChat = chatRepository.save(chat);

            log.info("AI角色切换成功: 聊天={}, 新角色={}, 聊天ID={}",
                    chatName, roleName, savedChat.getId());
            return savedChat;

        } catch (Exception e) {
            log.error("AI角色切换失败: 聊天={}, 角色={}, 错误信息={}",
                    chatName, roleName, e.getMessage(), e);
            throw e;
        }
    }
}
