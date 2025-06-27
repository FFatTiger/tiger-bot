package com.fffattiger.wechatbot.application.handler.cmd;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.CommandArgs;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.permission.service.PermissionDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 命令消息处理器
 */
@Slf4j
public class CommandMessageHandlerWrapper implements MessageHandlerExtension{
    private final CommandMessageHandlerExtension delegate;
    private final String commandPrefix;
    private final CommandRepository commandRepository;
    private final PermissionDomainService permissionDomainService;

    public CommandMessageHandlerWrapper(CommandMessageHandlerExtension delegate,
                                        CommandRepository commandRepository,
                                        PermissionDomainService permissionDomainService,
                                        String commandPrefix) {
        this.delegate = delegate;
        this.commandRepository = commandRepository;
        this.permissionDomainService = permissionDomainService;
        this.commandPrefix = commandPrefix;
    }

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String cleanContent = message.getCleanContent();
        String chatName = message.getChatName();
        String sender = message.getSenderName();

        // 1. 检查是否为可能需要本处理器处理的命令
        if (!isPotentialCommand(message, cleanContent)) {
            return false;
        }

        String commandName = extractCommandName(cleanContent);
        if (!delegate.getCommandName().equals(commandName)) {
            log.debug("命令处理器不匹配: handler={}, 命令={}", delegate.getClass().getSimpleName(), commandName);
            return false; // 不是我的命令，交给下一个处理器
        }

        log.info("命令处理器匹配: handler={}, 命令={}", delegate.getClass().getSimpleName(), commandName);
        
        // 2. 获取领域对象
        Command command = commandRepository.findByPattern(commandName).orElse(null);
        if (command == null) {
            log.warn("命令 '{}' 已被配置处理器，但在数据库中未找到定义。", commandName);
            context.replyText("这是一个未知的内部命令。");
            return true; // 确认处理，终止责任链
        }

        // // 3. 权限检查
        // if (!permissionDomainService.canExecuteCommand(Long.parseLong(message.getSenderId()), command.getId(), message.getChatId())) {
        //     log.warn("命令权限验证失败: 聊天={}, 发送者={}, 命令={}", chatName, sender, commandName);
        //     context.replyText("抱歉，您没有权限执行此命令。");
        //     return true; // 确认处理，终止责任链
        // }
        // log.info("命令权限验证通过: 聊天={}, 发送者={}, 命令={}", chatName, sender, commandName);

        // 4. 解析参数并执行
        try {
            CommandArgs commandArgs = command.extractCommandArgs(cleanContent, commandPrefix);
            
            long startTime = System.currentTimeMillis();
            delegate.doHandle(commandArgs.command(), commandArgs.args(), context);
            long duration = System.currentTimeMillis() - startTime;

            log.info("命令执行成功: 聊天={}, 发送者={}, 命令={}, 耗时={}ms",
                    chatName, sender, commandName, duration);

        } catch (Exception e) {
            log.error("命令执行异常: 聊天={}, 发送者={}, 命令={}, 错误信息={}",
                    chatName, sender, commandName, e.getMessage(), e);
            context.replyText("命令执行时发生未知错误。");
        }
        
        return true; // 无论成功失败，只要是我的命令，就终止责任链
    }
    
    private boolean isPotentialCommand(Message message, String content) {
        return "friend".equals(message.getAttr())
                && StringUtils.hasLength(content)
                && content.startsWith(commandPrefix);
    }

    private String extractCommandName(String content) {
        String commandStr = content.split(" ")[0];
        return commandStr.replace(commandPrefix, "");
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public CommandMessageHandlerExtension getDelegate() {
        return delegate;
    }
}