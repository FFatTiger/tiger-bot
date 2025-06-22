package com.fffattiger.wechatbot.application.handler.cmd;

import com.fffattiger.wechatbot.api.Ordered;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.application.service.CommandApplicationService;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.CommandArgs;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令消息处理器
 */
@Slf4j
public class CommandMessageHandlerWrapper implements MessageHandlerExtension{
    private final CommandMessageHandlerExtension delegate;
    private final String commandPrefix;
    private final CommandApplicationService commandApplicationService;

    public CommandMessageHandlerWrapper(CommandMessageHandlerExtension delegate, String commandPrefix, CommandApplicationService commandApplicationService) {
        this.delegate = delegate;
        this.commandPrefix = commandPrefix;
        this.commandApplicationService = commandApplicationService;
    }

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String cleanContent = message.getCleanContent();
        String chatName = message.getChatName();
        String sender = message.getSenderName();

        // 根据新的wxauto文档，检查是否为命令消息
        // 使用attr字段判断消息来源，"friend"表示好友/群友消息
        if (message.getAttr() == null || !"friend".equals(message.getAttr())
                || !StringUtils.hasLength(cleanContent)
                || !cleanContent.startsWith(commandPrefix)) {
            log.debug("非命令消息，跳过处理: 聊天={}, 发送者={}, 内容={}",
                    chatName, sender, cleanContent);
            return false;
        }

        boolean isCommand = false;
        String[] args = cleanContent.split(" ");
        String commandStr = args[0];
        String commandStrWithoutPrefix = commandStr.replace(commandPrefix, "");

        if (delegate.getCommandName().equals(commandStrWithoutPrefix)) {
            log.info("命令处理器匹配: handler={}, 命令={}", this.getClass().getSimpleName(), commandStr);
            Command command = commandApplicationService.findByCommand(commandStrWithoutPrefix);
            CommandArgs commandArgs = command.extractCommandArgs(cleanContent, commandPrefix);

            if (commandApplicationService.hasPermission(command, sender, message.getChatId())) {
                log.info("命令权限验证通过: 聊天={}, 发送者={}, 命令={}", chatName, sender, command);

                try {
                    long startTime = System.currentTimeMillis();
                    delegate.doHandle(commandArgs.command(), commandArgs.args(), context);
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("命令执行成功: 聊天={}, 发送者={}, 命令={}, 耗时={}ms",
                            chatName, sender, commandStr, duration);
                    isCommand = true;

                } catch (Exception e) {
                    log.error("命令执行异常: 聊天={}, 发送者={}, 命令={}, 错误信息={}",
                            chatName, sender, commandStr, e.getMessage(), e);
                    context.replyText("命令格式错误，请参考帮助");
                }
            } else {
                log.warn("命令权限验证失败: 聊天={}, 发送者={}, 命令={}", chatName, sender, commandStr);
            }
        } else {
            log.debug("命令处理器不匹配: handler={}, 命令={}", this.getClass().getSimpleName(), commandStr);
        }

        if (isCommand) {
            return true;
        }

        return false;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }

    public CommandMessageHandlerExtension getDelegate() {
        return delegate;
    }

   
}