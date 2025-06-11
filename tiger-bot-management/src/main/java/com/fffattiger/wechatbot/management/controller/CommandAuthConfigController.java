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

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.management.application.dto.CommandAuthConfigurationDto;
import com.fffattiger.wechatbot.management.application.service.CommandAuthManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令权限配置控制器
 */
@Controller
@RequestMapping("/management/bot-config")
@Slf4j
public class CommandAuthConfigController {

    @Autowired
    private CommandAuthManagementApplicationService commandAuthManagementApplicationService;

    /**
     * 命令权限配置页面
     */
    @GetMapping("/command-auths")
    public String commandAuths(Model model) {
        
        
        List<CommandAuthConfigurationDto> commandAuths = commandAuthManagementApplicationService.getAllCommandAuthConfigurations();
        List<Chat> allChats = commandAuthManagementApplicationService.getAllAvailableChats();
        List<Command> allCommands = commandAuthManagementApplicationService.getAllAvailableCommands();
        List<User> allUsers = commandAuthManagementApplicationService.getAllAvailableUsers();
        
        model.addAttribute("commandAuths", commandAuths);
        model.addAttribute("allChats", allChats);
        model.addAttribute("allCommands", allCommands);
        model.addAttribute("allUsers", allUsers);
        
        return "management/bot-config/command-auths";
    }

    /**
     * 创建命令权限
     */
    @PostMapping("/command-auths/create")
    @ResponseBody
    public String createCommandAuth(
            @RequestParam Long chatId,
            @RequestParam Long commandId,
            @RequestParam(required = false) Long userId) {
        
        try {
            CommandAuthConfigurationDto dto = CommandAuthConfigurationDto.forCreation(
                chatId, commandId, userId);
            
            commandAuthManagementApplicationService.createCommandAuthConfiguration(dto);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新命令权限
     */
    @PostMapping("/command-auths/{id}/update")
    @ResponseBody
    public String updateCommandAuth(
            @PathVariable Long id,
            @RequestParam Long chatId,
            @RequestParam Long commandId,
            @RequestParam(required = false) Long userId) {

        try {
            CommandAuthConfigurationDto dto = CommandAuthConfigurationDto.forCreation(
                chatId, commandId, userId);
            
            commandAuthManagementApplicationService.updateCommandAuthConfiguration(id, dto);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除命令权限
     */
    @PostMapping("/command-auths/{id}/delete")
    @ResponseBody
    public String deleteCommandAuth(@PathVariable Long id) {
        try {
            commandAuthManagementApplicationService.deleteCommandAuthConfiguration(id);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }
}
