package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

import java.util.List;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageType;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令消息处理器
 */
@Slf4j
public abstract class AbstractCommandMessageHandler implements MessageHandler {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        BatchedSanitizedWechatMessages.Chat.Message message = context.message();
        String sender = message.sender();
        String cleanContent = context.cleanContent();
        String commandPrefix = context.chatBotProperties().getCommandPrefix();
        if (message.type() == null || !message.type().equals(MessageType.FRIEND) || !StringUtils.hasLength(cleanContent)
                || !cleanContent.startsWith(commandPrefix)) {
            return chain.handle(context);
        }

        boolean isCommand = false;

        String[] args = cleanContent.split(" ");
        String command = args[0];
        String[] args2 = new String[args.length - 1];
        System.arraycopy(args, 1, args2, 0, args.length - 1);

        if (canHandle(cleanContent) && hasPermission(command, sender, context)) {
            log.debug("处理命令: {}, Handler: {}", cleanContent, this.getClass().getSimpleName());
            try {

                doHandle(command, args2, context);
                isCommand = true;
            } catch (Exception e) {
                log.error("命令处理失败, content: {}", cleanContent, e);
                context.wx().sendText(context.currentChat().chat().name(), "命令格式错误，请参考帮助");
            }
        }

        if (isCommand) {
            return true;
        }

        return chain.handle(context);

    }

    private boolean hasPermission(String cleanContent, String sender, MessageHandlerContext context) {
        List<ListenerAggregate.ChatCommandAuthWithCommandAndUser> commandAuthList = context.currentChat().commandAuths();
        if (commandAuthList.isEmpty()) {
            log.info("命令未开启, command: {}", cleanContent);
            return false;
        }
        boolean commandEnable = false;
        for (ListenerAggregate.ChatCommandAuthWithCommandAndUser commandAuth : commandAuthList) {
            String pattern = commandAuth.command().pattern();
            if (antPathMatcher.match(pattern, cleanContent)) {
                commandEnable = true;
                break;
            }
        }
        if (!commandEnable) {
            log.info("命令未开启, command: {}", cleanContent);
            return false;
        }

        for (ListenerAggregate.ChatCommandAuthWithCommandAndUser commandAuth : commandAuthList) {
            boolean hasPermission = false;
            if (antPathMatcher.match(commandAuth.command().pattern(), cleanContent)) {
                hasPermission = commandAuth.user().username().equals(sender);
            }
            if (hasPermission) {
                log.info("命令满足权限, command: {}, sender: {}", cleanContent, sender);
                return true;
            } else {
                log.info("命令不满足权限, command: {}, sender: {}", cleanContent, sender);
                context.wx().sendText(context.currentChat().chat().name(), "您无权限调用");
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 是否可以处理
     * 
     * @param command 命令
     * @return 是否可以处理
     */
    public abstract boolean canHandle(String command);

    /**
     * 处理
     * 
     * @param command 命令
     * @param context 上下文
     */
    public abstract void doHandle(String command, String[] args, MessageHandlerContext context);

    /**
     * 获取命令描述
     * 
     * @return 命令描述
     */
    public abstract String description();

}