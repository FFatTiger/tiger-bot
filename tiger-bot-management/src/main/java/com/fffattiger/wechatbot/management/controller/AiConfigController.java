package com.fffattiger.wechatbot.management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.management.application.dto.AiModelConfigurationDto;
import com.fffattiger.wechatbot.management.application.dto.AiProviderConfigurationDto;
import com.fffattiger.wechatbot.management.application.dto.AiRoleConfigurationDto;
import com.fffattiger.wechatbot.management.application.service.AiModelManagementApplicationService;
import com.fffattiger.wechatbot.management.application.service.AiProviderManagementApplicationService;
import com.fffattiger.wechatbot.management.application.service.AiRoleManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * AI配置管理控制器
 */
@Controller
@RequestMapping("/management/ai-config")
@Slf4j
public class AiConfigController {

    @Autowired
    private AiProviderManagementApplicationService aiProviderManagementApplicationService;

    @Autowired
    private AiModelManagementApplicationService aiModelManagementApplicationService;

    @Autowired
    private AiRoleManagementApplicationService aiRoleManagementApplicationService;

    // ==================== AI提供商管理 ====================

    /**
     * AI提供商管理页面
     */
    @GetMapping("/providers")
    public String providers(Model model) {
        

        List<AiProviderConfigurationDto> providers = aiProviderManagementApplicationService.getAllProviderConfigurations();
        List<String> supportedTypes = aiProviderManagementApplicationService.getSupportedProviderTypes();

        model.addAttribute("providers", providers);
        model.addAttribute("supportedTypes", supportedTypes);

        return "management/ai-config/providers";
    }

    /**
     * 创建AI提供商
     */
    @PostMapping("/providers/create")
    @ResponseBody
    public String createProvider(@RequestParam String providerType,
                                @RequestParam String providerName,
                                @RequestParam String apiKey,
                                @RequestParam String baseUrl) {
        try {
            AiProviderConfigurationDto dto = new AiProviderConfigurationDto(
                null, providerType, providerName, apiKey, baseUrl);
            aiProviderManagementApplicationService.createProviderConfiguration(dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新AI提供商
     */
    @PostMapping("/providers/{id}/update")
    @ResponseBody
    public String updateProvider(@PathVariable Long id,
                                @RequestParam String providerType,
                                @RequestParam String providerName,
                                @RequestParam String apiKey,
                                @RequestParam String baseUrl) {
        try {
            AiProviderConfigurationDto dto = new AiProviderConfigurationDto(
                id, providerType, providerName, apiKey, baseUrl);
            aiProviderManagementApplicationService.updateProviderConfiguration(id, dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除AI提供商
     */
    @PostMapping("/providers/{id}/delete")
    @ResponseBody
    public String deleteProvider(@PathVariable Long id) {
        try {
            aiProviderManagementApplicationService.deleteProviderConfiguration(id);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 测试AI提供商连接
     */
    @PostMapping("/providers/{id}/test")
    @ResponseBody
    public String testProvider(@PathVariable Long id) {
        try {
            boolean success = aiProviderManagementApplicationService.testProviderConnection(id);
            return success ? "success" : "failed";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 获取提供商类型的默认配置
     */
    @GetMapping("/providers/default-config")
    @ResponseBody
    public AiProviderConfigurationDto getDefaultProviderConfig(@RequestParam String providerType) {
        return aiProviderManagementApplicationService.getDefaultProviderConfiguration(providerType);
    }

    // ==================== AI模型管理 ====================

    /**
     * AI模型管理页面
     */
    @GetMapping("/models")
    public String models(Model model) {
        

        List<AiModelConfigurationDto> models = aiModelManagementApplicationService.getAllModelConfigurations();
        List<AiProvider> allProviders = aiModelManagementApplicationService.getAllAvailableProviders();

        model.addAttribute("models", models);
        model.addAttribute("allProviders", allProviders);

        return "management/ai-config/models";
    }

    /**
     * 创建AI模型
     */
    @PostMapping("/models/create")
    @ResponseBody
    public String createModel(@RequestParam Long aiProviderId,
                             @RequestParam String modelName,
                             @RequestParam String description,
                             @RequestParam int maxTokens,
                             @RequestParam int maxOutputTokens,
                             @RequestParam(defaultValue = "false") boolean reasoningFlg,
                             @RequestParam(defaultValue = "true") boolean streamFlg,
                             @RequestParam(defaultValue = "true") boolean enabled,
                             @RequestParam(defaultValue = "false") boolean toolCallFlg,
                             @RequestParam String params) {
        try {
            AiModelConfigurationDto dto = new AiModelConfigurationDto(
                null, aiProviderId, "", modelName, description, maxTokens, maxOutputTokens,
                reasoningFlg, streamFlg, enabled, toolCallFlg, params);
            aiModelManagementApplicationService.createModelConfiguration(dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新AI模型
     */
    @PostMapping("/models/{id}/update")
    @ResponseBody
    public String updateModel(@PathVariable Long id,
                             @RequestParam Long aiProviderId,
                             @RequestParam String modelName,
                             @RequestParam String description,
                             @RequestParam int maxTokens,
                             @RequestParam int maxOutputTokens,
                             @RequestParam(defaultValue = "false") boolean reasoningFlg,
                             @RequestParam(defaultValue = "true") boolean streamFlg,
                             @RequestParam(defaultValue = "true") boolean enabled,
                             @RequestParam(defaultValue = "false") boolean toolCallFlg,
                             @RequestParam String params) {
        try {
            AiModelConfigurationDto dto = new AiModelConfigurationDto(
                id, aiProviderId, "", modelName, description, maxTokens, maxOutputTokens,
                reasoningFlg, streamFlg, enabled, toolCallFlg, params);
            aiModelManagementApplicationService.updateModelConfiguration(id, dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除AI模型
     */
    @PostMapping("/models/{id}/delete")
    @ResponseBody
    public String deleteModel(@PathVariable Long id) {
        try {
            aiModelManagementApplicationService.deleteModelConfiguration(id);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 切换AI模型启用状态
     */
    @PostMapping("/models/{id}/toggle")
    @ResponseBody
    public String toggleModel(@PathVariable Long id) {
        try {
            aiModelManagementApplicationService.toggleModelEnabled(id);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 测试AI模型
     */
    @PostMapping("/models/{id}/test")
    @ResponseBody
    public String testModel(@PathVariable Long id) {
        try {
            boolean success = aiModelManagementApplicationService.testModel(id);
            return success ? "success" : "failed";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 获取模型的默认配置
     */
    @GetMapping("/models/default-config")
    @ResponseBody
    public AiModelConfigurationDto getDefaultModelConfig(@RequestParam Long providerId,
                                                         @RequestParam String providerType) {
        return aiModelManagementApplicationService.getDefaultModelConfiguration(providerId, providerType);
    }

    // ==================== AI角色管理 ====================

    /**
     * AI角色管理页面
     */
    @GetMapping("/roles")
    public String roles(Model model) {
        

        List<AiRoleConfigurationDto> roles = aiRoleManagementApplicationService.getAllRoleConfigurations();
        List<String> supportedPromptTypes = aiRoleManagementApplicationService.getSupportedPromptTypes();

        model.addAttribute("roles", roles);
        model.addAttribute("supportedPromptTypes", supportedPromptTypes);

        return "management/ai-config/roles";
    }

    /**
     * 创建AI角色
     */
    @PostMapping("/roles/create")
    @ResponseBody
    public String createRole(@RequestParam String name,
                            @RequestParam String promptContent,
                            @RequestParam(required = false) String extraMemory,
                            @RequestParam String promptType) {
        try {
            AiRoleConfigurationDto dto = new AiRoleConfigurationDto(
                null, name, promptContent, extraMemory, promptType);
            aiRoleManagementApplicationService.createRoleConfiguration(dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新AI角色
     */
    @PostMapping("/roles/{id}/update")
    @ResponseBody
    public String updateRole(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam String promptContent,
                            @RequestParam(required = false) String extraMemory,
                            @RequestParam String promptType) {
        try {
            AiRoleConfigurationDto dto = new AiRoleConfigurationDto(
                id, name, promptContent, extraMemory, promptType);
            aiRoleManagementApplicationService.updateRoleConfiguration(id, dto);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除AI角色
     */
    @PostMapping("/roles/{id}/delete")
    @ResponseBody
    public String deleteRole(@PathVariable Long id) {
        try {
            aiRoleManagementApplicationService.deleteRoleConfiguration(id);
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 预览AI角色效果
     */
    @PostMapping("/roles/preview")
    @ResponseBody
    public String previewRole(@RequestParam String name,
                             @RequestParam String promptContent,
                             @RequestParam(required = false) String extraMemory,
                             @RequestParam String promptType) {
        try {
            AiRoleConfigurationDto dto = new AiRoleConfigurationDto(
                null, name, promptContent, extraMemory, promptType);
            return aiRoleManagementApplicationService.previewRole(dto);
        } catch (Exception e) {
            
            return "预览失败: " + e.getMessage();
        }
    }

    /**
     * 验证提示词内容
     */
    @PostMapping("/roles/validate-prompt")
    @ResponseBody
    public String validatePromptContent(@RequestParam String promptContent) {
        String validationResult = aiRoleManagementApplicationService.validatePromptContent(promptContent);
        return validationResult != null ? validationResult : "valid";
    }

    /**
     * 检查角色名称是否已存在
     */
    @GetMapping("/roles/check-name")
    @ResponseBody
    public String checkRoleName(@RequestParam String name,
                               @RequestParam(required = false) Long excludeId) {
        if (excludeId != null) {
            boolean exists = aiRoleManagementApplicationService.isRoleNameExistsForOther(name, excludeId);
            return exists ? "exists" : "available";
        } else {
            boolean exists = aiRoleManagementApplicationService.isRoleNameExists(name);
            return exists ? "exists" : "available";
        }
    }

    /**
     * 获取默认的角色配置
     */
    @GetMapping("/roles/default-config")
    @ResponseBody
    public AiRoleConfigurationDto getDefaultRoleConfig() {
        return aiRoleManagementApplicationService.getDefaultRoleConfiguration();
    }
}
