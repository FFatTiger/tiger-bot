package com.fffattiger.wechatbot.application.handler.cmd;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChangeRoleCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private ChatApplicationService chatApplicationService;

    @Resource
    private AiRoleRepository aiRoleRepository;

    @Override
    public String getCommandName() {
        return "切换角色";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length == 0 || args[0].trim().isEmpty()) {
            context.replyText("请输入要切换的角色名称！\n\n使用方法：/切换角色 角色名称");
            return;
        }

        String roleName = args[0].trim();
        Long chatId = context.getMessage().getChatId();

        try {
            AiRole targetRole = aiRoleRepository.findByName(roleName)
                    .orElse(null);

            if (targetRole == null) {
                context.replyText("角色 '" + roleName + "' 不存在！\n\n请使用 /角色列表 查看所有可用角色。");
                return;
            }
            
            chatApplicationService.changeAiRole(chatId, targetRole.getId());

            log.info("用户 {} 在聊天 {} 中将角色切换为 {}", context.getMessage().getSenderName(), context.getMessage().getChatName(), roleName);
            context.replyText("✅ 角色已切换为: " + roleName);

        } catch (Exception e) {
            log.error("切换角色失败: {}", e.getMessage(), e);
            context.replyText("切换角色失败：" + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "/切换角色 <角色名称> - 切换AI角色";
    }
}
