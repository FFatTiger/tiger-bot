package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiRole;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClearMemoryCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private AiChatApplicationService aiChatApplicationService;

    @Resource
    private JdbcChatMemoryRepository chatMemoryRepository;

    @Override
    public String getCommandName() {
        return "清除记忆";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        String chatName = context.getMessage().chatName();
        
        try {
            if (args.length == 0 || !StringUtils.hasLength(args[0])) {
                // 清除当前角色的记忆
                clearCurrentRoleMemory(context, chatName);
            } else {
                // 清除指定角色的记忆
                String roleName = args[0];
                clearSpecificRoleMemory(context, chatName, roleName);
            }
            
        } catch (Exception e) {
            log.error("清除记忆失败: {}", e.getMessage(), e);
            context.replyText(chatName, "清除记忆失败：" + e.getMessage());
        }
    }

    /**
     * 清除当前角色的记忆
     */
    private void clearCurrentRoleMemory(MessageHandlerContext context, String chatName) {
        Long currentRoleId = context.get("currentRoleId");
        String conversationId = currentRoleId + "_" + chatName;
        
        chatMemoryRepository.deleteByConversationId(conversationId);
        
        
        context.replyText(chatName, "✅ 已清除当前角色的聊天记忆！");
    }

    /**
     * 清除指定角色的记忆
     */
    private void clearSpecificRoleMemory(MessageHandlerContext context, String chatName, String roleName) {
        // 查找指定角色
        AiRole role = aiChatApplicationService.getRoleByName(roleName)
                .orElse(null);
        if (role == null) {
            context.replyText(chatName, "角色 '" + roleName + "' 不存在，请使用 /角色列表 查看可用角色");
            return;
        }
        
        String conversationId = role.id() + "_" + chatName;
        chatMemoryRepository.deleteByConversationId(conversationId);
        
        
        context.replyText(chatName, "✅ 已清除角色 '" + roleName + "' 的聊天记忆！");
    }

    @Override
    public String getDescription() {
        return "/清除记忆 [角色名称] - 清除当前或指定角色的聊天记忆";
    }
}
