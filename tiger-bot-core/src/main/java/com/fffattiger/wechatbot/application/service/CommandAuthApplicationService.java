package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.repository.ChatCommandAuthRepository;
import com.fffattiger.wechatbot.domain.listener.service.ListenerDomainService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 命令权限应用服务
 */
@Service
@Slf4j
public class CommandAuthApplicationService {

    @Resource
    private ChatCommandAuthRepository chatCommandAuthRepository;

    @Resource
    private ChatApplicationService chatApplicationService;

    @Resource
    private CommandApplicationService commandApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private ListenerDomainService listenerDomainService;

    /**
     * 获取指定聊天的所有命令权限
     */
    public List<ChatCommandAuth> getCommandAuthsByChat(Long chatId) {
        return chatCommandAuthRepository.findByChatId(chatId);
    }

    /**
     * 获取所有命令权限
     */
    public List<ChatCommandAuth> getAllCommandAuths() {
        List<ChatCommandAuth> auths = new ArrayList<>();
        chatCommandAuthRepository.findAll().forEach(auths::add);
        return auths;
    }

    /**
     * 创建命令权限
     */
    public void createCommandAuth(Long chatId, Long commandId, Long userId) {
        // 使用应用服务验证相关实体存在
        chatApplicationService.getChatById(chatId);
        commandApplicationService.getCommandById(commandId);

        // 验证用户存在（如果指定了用户）
        if (userId != null) {
            userApplicationService.getUserById(userId);
        }

        ChatCommandAuth newAuth = new ChatCommandAuth(null, chatId, commandId, userId);

        // 使用领域对象验证
        if (!newAuth.isValidAuth()) {
            throw new RuntimeException("Invalid command auth data");
        }

        chatCommandAuthRepository.save(newAuth);
        
    }

    /**
     * 删除命令权限
     */
    public void deleteCommandAuth(Long authId) {
        chatCommandAuthRepository.deleteById(authId);
        
    }

    /**
     * 更新命令权限
     */
    public void updateCommandAuth(Long authId, Long chatId, Long commandId, Long userId) {
        // 验证权限记录存在
        chatCommandAuthRepository.findById(authId)
                .orElseThrow(() -> new RuntimeException("CommandAuth not found: " + authId));

        // 使用应用服务验证相关实体存在
        chatApplicationService.getChatById(chatId);
        commandApplicationService.getCommandById(commandId);
        if (userId != null) {
            userApplicationService.getUserById(userId);
        }

        ChatCommandAuth updatedAuth = new ChatCommandAuth(authId, chatId, commandId, userId);
        chatCommandAuthRepository.save(updatedAuth);
        
    }

    /**
     * 检查用户是否有权限执行命令
     * 委托给领域服务处理，避免重复逻辑
     */
    public boolean hasPermission(Long chatId, Long commandId, Long userId) {
        List<ChatCommandAuth> auths = chatCommandAuthRepository.findByChatIdAndCommandId(chatId, commandId);

        // 使用领域服务的权限检查逻辑
        return listenerDomainService.hasCommandPermission(auths, userId);
    }

    /**
     * 批量删除聊天的所有命令权限
     */
    public void deleteAllCommandAuthsByChat(Long chatId) {
        List<ChatCommandAuth> auths = chatCommandAuthRepository.findByChatId(chatId);
        chatCommandAuthRepository.deleteAll(auths);
        
    }
}
