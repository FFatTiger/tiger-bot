package com.fffattiger.wechatbot.application.handler.cmd;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.AiApplicationService;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;
import com.fffattiger.wechatbot.domain.chat.Chat;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClearMemoryCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private AiApplicationService aiApplicationService;

    @Resource
    private ChatApplicationService chatApplicationService;

    @Resource
    private AiRoleRepository aiRoleRepository;


    @Override
    public String getCommandName() {
        return "清除记忆";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        Long chatId = context.getMessage().getChatId();
        Chat chat = chatApplicationService.findById(chatId);
        
        try {
            if (args.length == 0 || !StringUtils.hasLength(args[0])) {
                clearCurrentRoleMemory(context, chat);
            } else {
                context.replyText("该命令暂不支持指定角色，已清除当前角色的聊天记忆。");
                clearCurrentRoleMemory(context, chat);
            }
            
        } catch (Exception e) {
            log.error("清除记忆失败: {}", e.getMessage(), e);
            context.replyText("清除记忆失败：" + e.getMessage());
        }
    }

    private void clearCurrentRoleMemory(MessageHandlerContext context, Chat chat) {
        Long currentRoleId = chat.getAiSpecification().aiRoleId();
        String conversationId = currentRoleId + "_" + chat.getName();
        
        aiApplicationService.clearMemory(conversationId);
        
        String roleName = aiRoleRepository.findById(currentRoleId)
                .map(AiRole::getName)
                .orElse("未知角色");

        context.replyText("✅ 已清除当前角色 (" + roleName + ") 的聊天记忆！");
    }

    @Override
    public String getDescription() {
        return "/清除记忆 - 清除当前角色的聊天记忆";
    }
}
