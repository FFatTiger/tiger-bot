package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.infrastructure.external.wchat.AbstractPredicateMessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessagePredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessagePredicateFactory;

/**
 * 基于谓词的状态命令处理器示例
 * 展示如何使用新的谓词模式
 */
@Service
public class PredicateStatusCommandMessageHandler extends AbstractPredicateMessageHandler {

    @Autowired
    private MessagePredicateFactory predicateFactory;

    @Override
    public MessagePredicate getPredicate() {
        // 使用谓词工厂创建复合条件：
        // 1. 必须是好友消息
        // 2. 必须有内容
        // 3. 必须以命令前缀开头
        // 4. 必须是状态命令
        return predicateFactory.friendMessage()
                .and(predicateFactory.hasContent())
                .and(predicateFactory.commandPrefix("/"))
                .and(context -> {
                    String content = context.cleanContent();
                    return content.startsWith("/status") || content.startsWith("/s");
                });
    }

    @Override
    public boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain) {
        // 解析命令参数
        String[] args = context.cleanContent().split(" ");
        String command = args[0];
        
        // 检查权限（这里简化处理，实际应该调用权限检查服务）
        if (!hasPermission(context)) {
            context.wx().sendText(context.currentChat().chat().getName(), "您无权限调用此命令");
            return true;
        }
        
        // 执行状态查询
        StringBuilder status = new StringBuilder();
        
        // 监听对象
        String listeners = context.wx().getListeners().stream()
                .collect(Collectors.joining(", "));
        status.append("监听对象: ").append(listeners).append("\n");
        
        // 当前聊天
        status.append("当前聊天: ").append(context.currentChat().chat().getName()).append("\n");
        
        // 机器人名称
        status.append("机器人名称: ").append(context.chatBotProperties().getRobotName()).append("\n");
        
        // 命令前缀
        status.append("命令前缀: ").append(context.chatBotProperties().getCommandPrefix()).append("\n");
        
        // TODO: 添加更多状态信息
        // - 今日回复数量
        // - 当前AI模型
        // - 当前角色设定
        // - 运行时间
        // - 系统版本
        
        context.wx().sendText(context.currentChat().chat().getName(), status.toString());
        
        return true; // 处理完成，不继续传递
    }
    
    /**
     * 简化的权限检查
     * 实际应用中应该调用权限服务
     */
    private boolean hasPermission(MessageHandlerContext context) {
        // 这里可以添加具体的权限检查逻辑
        // 例如检查发送者是否在管理员列表中
        return true; // 暂时允许所有用户
    }

    @Override
    public int getOrder() {
        return 0; // 高优先级
    }
    
    /**
     * 获取命令描述
     */
    public String getDescription() {
        return "/status, /s - 查看机器人状态";
    }
}
