package com.fffattiger.wechatbot.domain.listener.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.domain.message.Message;
import com.fffattiger.wechatbot.domain.user.User;

@Service
public class ListenerDomainService {

    /**
     * 检查是否可以创建监听器
     */
    public boolean canCreateListener(Chat chat, User creator) {
        if (chat == null || creator == null) {
            return false;
        }

        // 群聊需要管理员权限
        if (chat.isGroupChat() && !creator.hasRole("ADMIN")) {
            return false;
        }

        // 检查聊天对象是否有效
        if (!chat.canReceiveMessage()) {
            return false;
        }

        return true;
    }

    /**
     * 验证命令权限规则
     */
    public boolean validateCommandPermission(Listener listener, Command command, User user, Chat chat) {
        if (listener == null || command == null || chat == null) {
            return false;
        }

        // 检查命令是否有效
        if (!command.isValidCommand()) {
            return false;
        }

        // 检查用户是否有效
        if (user != null && !user.isValidUser()) {
            return false;
        }

        return true;
    }

    /**
     * 处理消息的业务逻辑
     */
    public MessageProcessingResult processMessage(Listener listener, Chat chat, Message message,
                                                String botName, List<ChatCommandAuth> commandAuths) {
        if (listener == null || chat == null || message == null) {
            return MessageProcessingResult.skip("参数无效");
        }

        // 检查消息是否有效
        if (!message.isValidMessage()) {
            return MessageProcessingResult.skip("消息无效");
        }

        // 检查是否应该处理该消息
        if (!listener.shouldProcessMessage(message.getContent(), botName, chat.isGroupChat())) {
            return MessageProcessingResult.skip("不满足处理条件");
        }

        return MessageProcessingResult.process();
    }

    /**
     * 检查用户是否有权限执行命令
     */
    public boolean hasCommandPermission(List<ChatCommandAuth> auths, Long userId) {
        if (auths == null || auths.isEmpty()) {
            return false;
        }

        // 检查是否有全局权限
        boolean hasGlobalPermission = auths.stream()
                .anyMatch(ChatCommandAuth::isGlobalPermission);

        if (hasGlobalPermission) {
            return true;
        }

        // 检查特定用户权限
        return auths.stream()
                .anyMatch(auth -> auth.allowsUser(userId));
    }

    /**
     * 消息处理结果
     */
    public static class MessageProcessingResult {
        private final boolean shouldProcess;
        private final String reason;

        private MessageProcessingResult(boolean shouldProcess, String reason) {
            this.shouldProcess = shouldProcess;
            this.reason = reason;
        }

        public static MessageProcessingResult process() {
            return new MessageProcessingResult(true, "");
        }

        public static MessageProcessingResult skip(String reason) {
            return new MessageProcessingResult(false, reason);
        }

        public boolean shouldProcess() {
            return shouldProcess;
        }

        public String getReason() {
            return reason;
        }
    }
}
