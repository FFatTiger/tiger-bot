package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.AiModelApplicationService;
import com.fffattiger.wechatbot.application.service.AiProviderApplicationService;
import com.fffattiger.wechatbot.application.service.AiRoleApplicationService;
import com.fffattiger.wechatbot.application.service.CommandApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.management.application.dto.CommandConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令管理应用服务
 * 负责命令配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class CommandManagementApplicationService {

    @Autowired
    private CommandApplicationService coreCommandApplicationService;

    @Autowired
    private AiProviderApplicationService coreAiProviderApplicationService;

    @Autowired
    private AiModelApplicationService coreAiModelApplicationService;

    @Autowired
    private AiRoleApplicationService coreAiRoleApplicationService;

    /**
     * 获取所有命令配置
     */
    public List<CommandConfigurationDto> getAllCommandConfigurations() {
        List<Command> commands = coreCommandApplicationService.getAllCommands();
        return commands.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取命令配置
     */
    public CommandConfigurationDto getCommandConfigurationById(Long id) {
        Command command = coreCommandApplicationService.getCommandById(id);
        return convertToDto(command);
    }

    /**
     * 创建命令配置
     */
    public void createCommandConfiguration(CommandConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("命令配置无效");
        }

        log.info("创建命令配置: {}", dto);

        // 构建AI配置
        AiSpecification aiSpecification = null;
        if (dto.hasCompleteAiConfiguration()) {
            aiSpecification = new AiSpecification(
                dto.aiProviderId(),
                dto.aiModelId(),
                dto.aiRoleId()
            );
        }

        // 调用核心模块的应用服务
        coreCommandApplicationService.createCommand(
            dto.pattern(),
            dto.description(),
            aiSpecification
        );
    }

    /**
     * 更新命令配置
     */
    public void updateCommandConfiguration(Long id, CommandConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("命令配置无效");
        }

        log.info("更新命令配置: {} -> {}", id, dto);

        // 构建AI配置
        AiSpecification aiSpecification = null;
        if (dto.hasCompleteAiConfiguration()) {
            aiSpecification = new AiSpecification(
                dto.aiProviderId(),
                dto.aiModelId(),
                dto.aiRoleId()
            );
        }

        // 调用核心模块的应用服务
        coreCommandApplicationService.updateCommand(
            id,
            dto.pattern(),
            dto.description(),
            aiSpecification
        );
    }

    /**
     * 删除命令配置
     */
    public void deleteCommandConfiguration(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("命令ID无效");
        }

        log.info("删除命令配置: {}", id);
        
        // 调用核心模块的应用服务
        coreCommandApplicationService.deleteCommand(id);
    }

    /**
     * 获取所有可用的AI提供商
     */
    public List<AiProvider> getAllAvailableAiProviders() {
        return coreAiProviderApplicationService.getAllProviders();
    }

    /**
     * 获取所有可用的AI模型
     */
    public List<AiModel> getAllAvailableAiModels() {
        return coreAiModelApplicationService.getAllModels();
    }

    /**
     * 获取所有可用的AI角色
     */
    public List<AiRole> getAllAvailableAiRoles() {
        return coreAiRoleApplicationService.getAllRoles();
    }

    /**
     * 转换为DTO
     */
    private CommandConfigurationDto convertToDto(Command command) {
        String aiProviderName = null;
        String aiModelName = null;
        String aiRoleName = null;
        boolean hasAiConfiguration = false;

        if (command.getAiSpecification() != null) {
            AiSpecification aiSpec = command.getAiSpecification();
            hasAiConfiguration = true;

            // 获取AI提供商名称
            if (aiSpec.aiProviderId() != null) {
                try {
                    AiProvider provider = coreAiProviderApplicationService.getProviderById(aiSpec.aiProviderId())
                            .orElse(null);
                    if (provider != null) {
                        aiProviderName = provider.providerName();
                    }
                } catch (Exception e) {
                    log.warn("获取AI提供商名称失败: {}", aiSpec.aiProviderId(), e);
                }
            }

            // 获取AI模型名称
            if (aiSpec.aiModelId() != null) {
                try {
                    AiModel model = coreAiModelApplicationService.getModelById(aiSpec.aiModelId())
                            .orElse(null);
                    if (model != null) {
                        aiModelName = model.modelName();
                    }
                } catch (Exception e) {
                    log.warn("获取AI模型名称失败: {}", aiSpec.aiModelId(), e);
                }
            }

            // 获取AI角色名称
            if (aiSpec.aiRoleId() != null) {
                try {
                    AiRole role = coreAiRoleApplicationService.getRoleById(aiSpec.aiRoleId())
                            .orElse(null);
                    if (role != null) {
                        aiRoleName = role.name();
                    }
                } catch (Exception e) {
                    log.warn("获取AI角色名称失败: {}", aiSpec.aiRoleId(), e);
                }
            }
        }

        return new CommandConfigurationDto(
            command.getId(),
            command.getPattern(),
            command.getDescription(),
            command.getAiSpecification() != null ? command.getAiSpecification().aiProviderId() : null,
            aiProviderName,
            command.getAiSpecification() != null ? command.getAiSpecification().aiModelId() : null,
            aiModelName,
            command.getAiSpecification() != null ? command.getAiSpecification().aiRoleId() : null,
            aiRoleName,
            hasAiConfiguration
        );
    }
}
