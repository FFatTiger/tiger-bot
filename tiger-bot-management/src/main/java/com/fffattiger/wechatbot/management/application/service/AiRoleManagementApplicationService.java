package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.AiRoleApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.management.application.dto.AiRoleConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * AI角色管理应用服务
 * 负责AI角色配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class AiRoleManagementApplicationService {

    @Autowired
    private AiRoleApplicationService coreAiRoleApplicationService;

    /**
     * 获取所有AI角色配置
     */
    public List<AiRoleConfigurationDto> getAllRoleConfigurations() {
        List<AiRole> roles = coreAiRoleApplicationService.getAllRoles();
        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取AI角色配置
     */
    public AiRoleConfigurationDto getRoleConfigurationById(Long id) {
        AiRole role = coreAiRoleApplicationService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("AI角色不存在: " + id));
        return convertToDto(role);
    }

    /**
     * 创建AI角色配置
     */
    public void createRoleConfiguration(AiRoleConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI角色配置无效");
        }

        log.info("创建AI角色配置: {}", dto);

        // 调用核心模块的应用服务
        coreAiRoleApplicationService.createRole(
            dto.name(),
            dto.promptContent(),
            dto.extraMemory(),
            dto.promptType()
        );
    }

    /**
     * 更新AI角色配置
     */
    public void updateRoleConfiguration(Long id, AiRoleConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI角色配置无效");
        }

        log.info("更新AI角色配置: {} -> {}", id, dto);

        // 调用核心模块的应用服务
        coreAiRoleApplicationService.updateRole(
            id,
            dto.name(),
            dto.promptContent(),
            dto.extraMemory(),
            dto.promptType()
        );
    }

    /**
     * 删除AI角色配置
     */
    public void deleteRoleConfiguration(Long id) {
        log.info("删除AI角色配置: {}", id);

        // 调用核心模块的应用服务
        coreAiRoleApplicationService.deleteRole(id);
    }

    /**
     * 检查角色名称是否已存在
     */
    public boolean isRoleNameExists(String name) {
        return coreAiRoleApplicationService.existsByName(name);
    }

    /**
     * 检查角色名称是否已被其他角色使用
     */
    public boolean isRoleNameExistsForOther(String name, Long excludeId) {
        if (!isRoleNameExists(name)) {
            return false;
        }
        
        AiRole existingRole = coreAiRoleApplicationService.getRoleByName(name).orElse(null);
        return existingRole != null && !existingRole.id().equals(excludeId);
    }

    /**
     * 获取支持的提示词类型列表
     */
    public List<String> getSupportedPromptTypes() {
        return List.of("system", "user", "assistant", "function");
    }

    /**
     * 获取默认的角色配置
     */
    public AiRoleConfigurationDto getDefaultRoleConfiguration() {
        String defaultPromptContent = """
            # Role: 智能助手
            
            ## Profile
            - language: 中文
            - description: 一个友善、专业的AI助手
            - personality: 耐心、细致、乐于助人
            
            ## Skills
            1. 回答各种问题
            2. 提供建议和帮助
            3. 进行友好的对话
            
            ## Rules
            1. 保持礼貌和专业
            2. 提供准确的信息
            3. 承认不确定的内容
            
            ## Initialization
            你好！我是你的AI助手，很高兴为你服务。有什么我可以帮助你的吗？
            """;

        return new AiRoleConfigurationDto(
            null,
            "",
            defaultPromptContent,
            "",
            "system"
        );
    }

    /**
     * 预览角色效果（模拟测试）
     */
    public String previewRole(AiRoleConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            return "角色配置无效，无法预览";
        }

        log.info("预览AI角色效果: {}", dto.name());

        // TODO: 实现实际的角色预览逻辑
        // 这里可以使用角色配置生成一个简单的测试对话
        
        StringBuilder preview = new StringBuilder();
        preview.append("角色名称: ").append(dto.name()).append("\n");
        preview.append("提示词类型: ").append(dto.getPromptTypeDisplayName()).append("\n");
        preview.append("角色描述: ").append(dto.getRoleDescription()).append("\n");
        
        if (dto.hasExtraMemory()) {
            preview.append("额外记忆: ").append(dto.getExtraMemoryPreview()).append("\n");
        }
        
        preview.append("\n模拟对话:\n");
        preview.append("用户: 你好\n");
        preview.append("助手: 你好！我是").append(dto.name()).append("，很高兴见到你！有什么我可以帮助你的吗？");
        
        return preview.toString();
    }

    /**
     * 验证提示词内容
     */
    public String validatePromptContent(String promptContent) {
        if (promptContent == null || promptContent.trim().isEmpty()) {
            return "提示词内容不能为空";
        }
        
        String content = promptContent.trim();
        
        if (content.length() < 10) {
            return "提示词内容过短，建议至少包含10个字符";
        }
        
        if (content.length() > 10000) {
            return "提示词内容过长，建议不超过10000个字符";
        }
        
        // 检查是否包含基本的角色定义
        if (!content.toLowerCase().contains("role") && 
            !content.contains("角色") && 
            !content.contains("助手") && 
            !content.contains("机器人")) {
            return "建议在提示词中明确定义角色身份";
        }
        
        return null; // 验证通过
    }

    /**
     * 将领域对象转换为DTO
     */
    private AiRoleConfigurationDto convertToDto(AiRole role) {
        return new AiRoleConfigurationDto(
            role.id(),
            role.name(),
            role.promptContent(),
            role.extraMemory(),
            role.promptType()
        );
    }
}
