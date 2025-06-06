package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.repository.ChatCommandAuthRepository;
import com.fffattiger.wechatbot.domain.user.repository.UserRepository;

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
    private ChatRepository chatRepository;

    @Resource
    private CommandRepository commandRepository;

    @Resource
    private UserRepository userRepository;

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
        // 验证聊天对象存在
        chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
        
        // 验证命令存在
        commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found: " + commandId));
        
        // 验证用户存在（如果指定了用户）
        if (userId != null) {
            userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        }

        ChatCommandAuth newAuth = new ChatCommandAuth(null, chatId, commandId, userId);
        chatCommandAuthRepository.save(newAuth);
        log.info("创建命令权限: {}", newAuth);
    }

    /**
     * 删除命令权限
     */
    public void deleteCommandAuth(Long authId) {
        chatCommandAuthRepository.deleteById(authId);
        log.info("删除命令权限: {}", authId);
    }

    /**
     * 更新命令权限
     */
    public void updateCommandAuth(Long authId, Long chatId, Long commandId, Long userId) {
        // 验证权限记录存在
        chatCommandAuthRepository.findById(authId)
                .orElseThrow(() -> new RuntimeException("CommandAuth not found: " + authId));

        // 验证相关实体存在
        chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
        commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found: " + commandId));
        if (userId != null) {
            userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        }

        ChatCommandAuth updatedAuth = new ChatCommandAuth(authId, chatId, commandId, userId);
        chatCommandAuthRepository.save(updatedAuth);
        log.info("更新命令权限: {}", updatedAuth);
    }

    /**
     * 检查用户是否有权限执行命令
     */
    public boolean hasPermission(Long chatId, Long commandId, Long userId) {
        List<ChatCommandAuth> auths = chatCommandAuthRepository.findByChatIdAndCommandId(chatId, commandId);
        
        // 如果没有任何权限配置，默认拒绝
        if (auths.isEmpty()) {
            return false;
        }
        
        // 检查是否有全局权限（userId为null）
        boolean hasGlobalPermission = auths.stream()
                .anyMatch(auth -> auth.userId() == null);
        
        if (hasGlobalPermission) {
            return true;
        }
        
        // 检查是否有特定用户权限
        return auths.stream()
                .anyMatch(auth -> userId.equals(auth.userId()));
    }

    /**
     * 批量删除聊天的所有命令权限
     */
    public void deleteAllCommandAuthsByChat(Long chatId) {
        List<ChatCommandAuth> auths = chatCommandAuthRepository.findByChatId(chatId);
        chatCommandAuthRepository.deleteAll(auths);
        log.info("删除聊天 {} 的所有命令权限", chatId);
    }
}
