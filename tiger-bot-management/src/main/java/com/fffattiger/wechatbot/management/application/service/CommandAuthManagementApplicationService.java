package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.application.service.CommandApplicationService;
import com.fffattiger.wechatbot.application.service.CommandAuthApplicationService;
import com.fffattiger.wechatbot.application.service.UserApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.management.application.dto.CommandAuthConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令权限管理应用服务
 * 负责命令权限配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class CommandAuthManagementApplicationService {

    @Autowired
    private CommandAuthApplicationService coreCommandAuthApplicationService;

    @Autowired
    private ChatApplicationService coreChatApplicationService;

    @Autowired
    private CommandApplicationService coreCommandApplicationService;

    @Autowired
    private UserApplicationService coreUserApplicationService;

    /**
     * 获取所有命令权限配置
     */
    public List<CommandAuthConfigurationDto> getAllCommandAuthConfigurations() {
        List<ChatCommandAuth> commandAuths = coreCommandAuthApplicationService.getAllCommandAuths();
        List<Chat> allChats = coreChatApplicationService.getAllChats();
        List<Command> allCommands = coreCommandApplicationService.getAllCommands();
        List<User> allUsers = coreUserApplicationService.getAllUsers();

        return commandAuths.stream()
                .map(auth -> convertToDto(auth, allChats, allCommands, allUsers))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的聊天对象
     */
    public List<Chat> getAllAvailableChats() {
        return coreChatApplicationService.getAllChats();
    }

    /**
     * 获取所有可用的命令
     */
    public List<Command> getAllAvailableCommands() {
        return coreCommandApplicationService.getAllCommands();
    }

    /**
     * 获取所有可用的用户
     */
    public List<User> getAllAvailableUsers() {
        return coreUserApplicationService.getAllUsers();
    }

    /**
     * 创建命令权限配置
     */
    public void createCommandAuthConfiguration(CommandAuthConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("命令权限配置无效");
        }

        log.info("创建命令权限配置: {}", dto);
        
        // 调用核心模块的应用服务
        coreCommandAuthApplicationService.createCommandAuth(
            dto.chatId(),
            dto.commandId(),
            dto.userId()
        );
    }

    /**
     * 更新命令权限配置
     */
    public void updateCommandAuthConfiguration(Long id, CommandAuthConfigurationDto dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("命令权限ID无效");
        }
        
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("命令权限配置无效");
        }

        log.info("更新命令权限配置: id={}, dto={}", id, dto);
        
        // 调用核心模块的应用服务
        coreCommandAuthApplicationService.updateCommandAuth(
            id,
            dto.chatId(),
            dto.commandId(),
            dto.userId()
        );
    }

    /**
     * 删除命令权限配置
     */
    public void deleteCommandAuthConfiguration(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("命令权限ID无效");
        }

        log.info("删除命令权限配置: {}", id);
        
        // 调用核心模块的应用服务
        coreCommandAuthApplicationService.deleteCommandAuth(id);
    }

    /**
     * 转换为DTO
     */
    private CommandAuthConfigurationDto convertToDto(ChatCommandAuth auth, 
                                                   List<Chat> chats, 
                                                   List<Command> commands, 
                                                   List<User> users) {
        String chatName = chats.stream()
                .filter(chat -> chat.getId().equals(auth.getChatId()))
                .map(Chat::getName)
                .findFirst()
                .orElse("未知聊天");
                
        String commandName = commands.stream()
                .filter(command -> command.getId().equals(auth.getCommandId()))
                .map(Command::getPattern)
                .findFirst()
                .orElse("未知命令");
                
        String username = null;
        if (auth.getUserId() != null) {
            username = users.stream()
                    .filter(user -> user.getId().equals(auth.getUserId()))
                    .map(User::getUsername)
                    .findFirst()
                    .orElse("未知用户");
        }
        
        return new CommandAuthConfigurationDto(
            auth.getId(),
            auth.getChatId(),
            chatName,
            auth.getCommandId(),
            commandName,
            auth.getUserId(),
            username
        );
    }
}
