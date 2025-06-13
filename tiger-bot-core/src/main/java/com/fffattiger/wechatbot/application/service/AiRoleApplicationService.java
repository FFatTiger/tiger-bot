package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI角色应用服务
 * 负责AI角色的业务逻辑处理
 */
@Service
@Slf4j
public class AiRoleApplicationService {

    @Resource
    private AiRoleRepository aiRoleRepository;

    /**
     * 获取所有AI角色
     */
    public List<AiRole> getAllRoles() {
        List<AiRole> roles = new ArrayList<>();
        aiRoleRepository.findAll().forEach(roles::add);
        log.debug("获取所有AI角色，共{}个", roles.size());
        return roles;
    }

    /**
     * 根据ID获取AI角色
     */
    public Optional<AiRole> getRoleById(Long id) {
        return aiRoleRepository.findById(id);
    }

    /**
     * 根据名称获取AI角色
     */
    public Optional<AiRole> getRoleByName(String name) {
        return getAllRoles().stream()
                .filter(role -> role.name().equals(name))
                .findFirst();
    }

    /**
     * 创建AI角色
     */
    public AiRole createRole(String name, String promptContent, String extraMemory, String promptType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (promptContent == null || promptContent.trim().isEmpty()) {
            throw new IllegalArgumentException("提示词内容不能为空");
        }
        if (promptType == null || promptType.trim().isEmpty()) {
            throw new IllegalArgumentException("提示词类型不能为空");
        }

        // 检查角色名称是否已存在
        if (getRoleByName(name.trim()).isPresent()) {
            throw new IllegalArgumentException("角色名称已存在: " + name.trim());
        }

        AiRole newRole = new AiRole(null, name.trim(), promptContent.trim(), 
                                   extraMemory != null ? extraMemory.trim() : "", promptType.trim());
        AiRole savedRole = aiRoleRepository.save(newRole);
        
        return savedRole;
    }

    /**
     * 更新AI角色
     */
    public AiRole updateRole(Long id, String name, String promptContent, String extraMemory, String promptType) {
        aiRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI角色不存在: " + id));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (promptContent == null || promptContent.trim().isEmpty()) {
            throw new IllegalArgumentException("提示词内容不能为空");
        }
        if (promptType == null || promptType.trim().isEmpty()) {
            throw new IllegalArgumentException("提示词类型不能为空");
        }

        // 检查角色名称是否已被其他角色使用
        Optional<AiRole> existingRoleWithSameName = getRoleByName(name.trim());
        if (existingRoleWithSameName.isPresent() && !existingRoleWithSameName.get().id().equals(id)) {
            throw new IllegalArgumentException("角色名称已存在: " + name.trim());
        }

        AiRole updatedRole = new AiRole(id, name.trim(), promptContent.trim(),
                                       extraMemory != null ? extraMemory.trim() : "", promptType.trim());
        AiRole savedRole = aiRoleRepository.save(updatedRole);
        
        return savedRole;
    }

    /**
     * 删除AI角色
     */
    public void deleteRole(Long id) {
        if (!aiRoleRepository.existsById(id)) {
            throw new RuntimeException("AI角色不存在: " + id);
        }
        
        aiRoleRepository.deleteById(id);
        
    }

    /**
     * 检查角色是否存在
     */
    public boolean existsById(Long id) {
        return aiRoleRepository.existsById(id);
    }

    /**
     * 检查角色名称是否已存在
     */
    public boolean existsByName(String name) {
        return getRoleByName(name).isPresent();
    }

    /**
     * 获取角色的简短描述（从提示词内容中提取前100个字符）
     */
    public String getRoleDescription(AiRole role) {
        if (role.promptContent() == null || role.promptContent().isEmpty()) {
            return "无描述";
        }
        
        String content = role.promptContent().trim();
        if (content.length() <= 100) {
            return content;
        }
        
        return content.substring(0, 100) + "...";
    }

    /**
     * 验证提示词类型
     */
    public boolean isValidPromptType(String promptType) {
        if (promptType == null || promptType.trim().isEmpty()) {
            return false;
        }
        
        // 常见的提示词类型
        List<String> validTypes = List.of("system", "user", "assistant", "function");
        return validTypes.contains(promptType.trim().toLowerCase());
    }
}
