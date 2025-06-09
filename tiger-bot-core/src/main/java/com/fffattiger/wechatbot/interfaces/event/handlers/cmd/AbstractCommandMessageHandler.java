package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

import java.util.List;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
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
                context.wx().sendText(context.currentChat().chat().getName(), "命令格式错误，请参考帮助");
            }
        }

        if (isCommand) {
            return true;
        }

        return chain.handle(context);

    }

    private boolean hasPermission(String cleanContent, String sender, MessageHandlerContext context) {
        List<MessageProcessingData.ChatCommandAuthWithCommandAndUser> commandAuthList = context.currentChat().commandAuths();
        if (commandAuthList.isEmpty()) {
            log.info("命令未开启, command: {}", cleanContent);
            return false;
        }

        // 使用富领域模型的业务方法检查命令匹配和权限
        boolean hasPermission = false;
        for (MessageProcessingData.ChatCommandAuthWithCommandAndUser commandAuth : commandAuthList) {
            // 使用富领域模型的业务方法检查命令匹配
            if (commandAuth.command().matches(cleanContent)) {
                // 使用富领域模型验证命令有效性
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
            log.info("命令权限不足, command: {}, sender: {}", cleanContent, sender);
            context.wx().sendText(context.currentChat().chat().getName(), "您无权限调用此命令");
            return false;
        }

        log.info("命令权限验证通过, command: {}, sender: {}", cleanContent, sender);
        return true;
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