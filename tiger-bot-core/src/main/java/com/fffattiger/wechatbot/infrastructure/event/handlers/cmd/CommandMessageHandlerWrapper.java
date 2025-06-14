package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import java.util.List;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.Message;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.application.dto.MessageProcessingData.ChatCommandAuthWithCommandAndUser;
import com.fffattiger.wechatbot.application.service.CommandAuthApplicationService;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.application.service.CommandApplicationService;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令消息处理器
 */
@Slf4j
public class CommandMessageHandlerWrapper implements MessageHandlerExtension {
    private final CommandMessageHandlerExtension delegate;
    private final String commandPrefix;

    public CommandMessageHandlerWrapper(CommandMessageHandlerExtension delegate, String commandPrefix) {
        this.delegate = delegate;
        this.commandPrefix = commandPrefix;
    }

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String cleanContent = message.cleanContent();
        String chatName = message.chatName();
        String sender = message.senderName();

        // 检查是否为命令消息
        if (message.type() == null || !message.type().equals(MessageType.FRIEND.getValue())
                || !StringUtils.hasLength(cleanContent)
                || !cleanContent.startsWith(commandPrefix)) {
            log.debug("非命令消息，跳过处理: 聊天={}, 发送者={}, 内容={}",
                    chatName, sender, cleanContent);
            return false;
        }

        boolean isCommand = false;
        String[] args = cleanContent.split(" ");
        String command = args[0];
        String[] args2 = new String[args.length - 1];
        System.arraycopy(args, 1, args2, 0, args.length - 1);

        if (delegate.getCommandName().equals(command.replace(commandPrefix, ""))) {
            log.info("命令处理器匹配: handler={}, 命令={}", this.getClass().getSimpleName(), command);

            if (hasPermission(command, sender, context)) {
                log.info("命令权限验证通过: 聊天={}, 发送者={}, 命令={}", chatName, sender, command);

                try {
                    long startTime = System.currentTimeMillis();
                    delegate.doHandle(command, args2, context);
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("命令执行成功: 聊天={}, 发送者={}, 命令={}, 耗时={}ms",
                            chatName, sender, command, duration);
                    isCommand = true;

                } catch (Exception e) {
                    log.error("命令执行异常: 聊天={}, 发送者={}, 命令={}, 错误信息={}",
                            chatName, sender, command, e.getMessage(), e);
                    context.replyText("命令格式错误，请参考帮助");
                }
            } else {
                log.warn("命令权限验证失败: 聊天={}, 发送者={}, 命令={}", chatName, sender, command);
            }
        } else {
            log.debug("命令处理器不匹配: handler={}, 命令={}", this.getClass().getSimpleName(), command);
        }

        if (isCommand) {
            return true;
        }

        return false;
    }


    private boolean hasPermission(String cleanContent, String sender, MessageHandlerContext context) {
        List<ChatCommandAuthWithCommandAndUser> commandAuths = (List<ChatCommandAuthWithCommandAndUser>) context.get("commandAuths");
        if (commandAuths.isEmpty()) {
            return false;
        }
        

        boolean hasPermission = false;
        for (ChatCommandAuthWithCommandAndUser commandAuth : commandAuths) {

            if (commandAuth.command().matches(cleanContent)) {
                if (!commandAuth.command().isValidCommand()) {
                    log.warn("命令无效: {}", commandAuth.command().getPattern());
                    continue;
                }

                // 检查权限
                if (commandAuth.auth().isGlobalPermission()) {
                    hasPermission = true;
                    break;
                } else if (commandAuth.user() != null && commandAuth.user().isValidUser()) {
                    if (commandAuth.user().getUsername().equals(sender)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
        }

        if (!hasPermission) {
            log.warn("用户无权限执行命令: 发送者={}, 命令={}", sender, cleanContent);
            context.replyText("您无权限调用此命令");
            return false;
        }

        log.debug("命令权限验证通过: 发送者={}, 命令={}", sender, cleanContent);
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

   
}