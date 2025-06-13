package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChangeRoleCommandMessageHandler extends AbstractCommandMessageHandler {

    @Resource
    private AiChatApplicationService aiChatApplicationService;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/切换角色");
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        String chatName = context.currentChat().chat().getName();

        // 检查参数
        if (args.length == 0 || !StringUtils.hasLength(args[0])) {
            log.info("角色切换命令缺少参数: 聊天={}", chatName);
            context.wx().sendText(chatName, "请指定角色名称，例如：/切换角色 贴吧老哥");
            return;
        }

        String roleName = args[0];
        log.info("执行角色切换命令: 聊天={}, 目标角色={}", chatName, roleName);

        try {
            // 查询新角色是否存在
            AiRole role = aiChatApplicationService.getRoleByName(roleName)
                    .orElse(null);
            if (role == null) {
                log.warn("角色不存在: 聊天={}, 角色名称={}", chatName, roleName);
                context.wx().sendText(chatName, "角色 '" + roleName + "' 不存在，请使用 /角色列表 查看可用角色");
                return;
            }

            // 切换角色
            aiChatApplicationService.changeRole(chatName, roleName);

            log.info("角色切换成功: 聊天={}, 新角色={}", chatName, roleName);
            context.wx().sendText(chatName, "已成功切换到角色：" + roleName + "\n" +
                    "💡 提示：不同角色的聊天记忆是独立的，您可以继续之前与该角色的对话！");

        } catch (Exception e) {
            log.error("角色切换失败: 聊天={}, 角色={}, 错误信息={}", chatName, roleName, e.getMessage(), e);
            context.wx().sendText(chatName, "切换角色失败：" + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "/切换角色 角色名称 - 切换AI角色（保留各角色独立记忆）";
    }
}
