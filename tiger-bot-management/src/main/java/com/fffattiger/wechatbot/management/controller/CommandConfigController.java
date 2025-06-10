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

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.management.application.dto.CommandConfigurationDto;
import com.fffattiger.wechatbot.management.application.service.CommandManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令配置控制器
 */
@Controller
@RequestMapping("/management/command-config")
@Slf4j
public class CommandConfigController {

    @Autowired
    private CommandManagementApplicationService commandManagementApplicationService;

    /**
     * 命令配置页面
     */
    @GetMapping("/commands")
    public String commands(Model model) {
        log.info("访问命令配置页面");
        
        List<CommandConfigurationDto> commands = commandManagementApplicationService.getAllCommandConfigurations();
        List<AiProvider> allProviders = commandManagementApplicationService.getAllAvailableAiProviders();
        List<AiModel> allModels = commandManagementApplicationService.getAllAvailableAiModels();
        List<AiRole> allRoles = commandManagementApplicationService.getAllAvailableAiRoles();
        
        model.addAttribute("commands", commands);
        model.addAttribute("allProviders", allProviders);
        model.addAttribute("allModels", allModels);
        model.addAttribute("allRoles", allRoles);
        
        return "management/command-config/commands";
    }

    /**
     * 创建命令
     */
    @PostMapping("/commands/create")
    @ResponseBody
    public String createCommand(
            @RequestParam String pattern,
            @RequestParam String description,
            @RequestParam(required = false) Long aiProviderId,
            @RequestParam(required = false) Long aiModelId,
            @RequestParam(required = false) Long aiRoleId) {
        
        try {
            CommandConfigurationDto dto = CommandConfigurationDto.forCreation(
                pattern, description, aiProviderId, aiModelId, aiRoleId);
            
            commandManagementApplicationService.createCommandConfiguration(dto);
            log.info("创建命令成功: pattern={}, description={}", pattern, description);
            return "success";
        } catch (Exception e) {
            log.error("创建命令失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新命令
     */
    @PostMapping("/commands/{id}/update")
    @ResponseBody
    public String updateCommand(
            @PathVariable Long id,
            @RequestParam String pattern,
            @RequestParam String description,
            @RequestParam(required = false) Long aiProviderId,
            @RequestParam(required = false) Long aiModelId,
            @RequestParam(required = false) Long aiRoleId) {

        try {
            CommandConfigurationDto dto = CommandConfigurationDto.forCreation(
                pattern, description, aiProviderId, aiModelId, aiRoleId);
            
            commandManagementApplicationService.updateCommandConfiguration(id, dto);
            log.info("更新命令成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("更新命令失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除命令
     */
    @PostMapping("/commands/{id}/delete")
    @ResponseBody
    public String deleteCommand(@PathVariable Long id) {
        try {
            commandManagementApplicationService.deleteCommandConfiguration(id);
            log.info("删除命令成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("删除命令失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 获取命令详情（用于编辑）
     */
    @GetMapping("/commands/{id}")
    @ResponseBody
    public CommandConfigurationDto getCommand(@PathVariable Long id) {
        try {
            return commandManagementApplicationService.getCommandConfigurationById(id);
        } catch (Exception e) {
            log.error("获取命令详情失败", e);
            return null;
        }
    }
}
