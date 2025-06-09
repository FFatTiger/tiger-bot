package com.fffattiger.wechatbot.infrastructure.external.wchat;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.CommandPrefixPredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.ContentMatchesPredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.GroupChatPredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.HasContentPredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.MessageTypePredicate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.predicates.SenderPredicate;

/**
 * 消息谓词工厂
 * 提供常用谓词的创建方法和组合谓词
 */
@Component
public class MessagePredicateFactory {
    
    /**
     * 创建消息类型谓词
     * @param type 消息类型
     * @return 消息类型谓词
     */
    public MessagePredicate messageType(MessageType type) {
        return new MessageTypePredicate(type);
    }
    
    /**
     * 创建命令前缀谓词
     * @param prefix 命令前缀
     * @return 命令前缀谓词
     */
    public MessagePredicate commandPrefix(String prefix) {
        return new CommandPrefixPredicate(prefix);
    }
    
    /**
     * 创建群聊谓词
     * @return 群聊谓词
     */
    public MessagePredicate groupChat() {
        return new GroupChatPredicate();
    }
    
    /**
     * 创建有内容谓词
     * @return 有内容谓词
     */
    public MessagePredicate hasContent() {
        return new HasContentPredicate();
    }
    
    /**
     * 创建发送者谓词
     * @param sender 发送者名称
     * @return 发送者谓词
     */
    public MessagePredicate sender(String sender) {
        return new SenderPredicate(sender);
    }
    
    /**
     * 创建内容匹配谓词
     * @param regex 正则表达式
     * @return 内容匹配谓词
     */
    public MessagePredicate contentMatches(String regex) {
        return new ContentMatchesPredicate(regex);
    }
    
    /**
     * 创建内容包含谓词
     * @param keyword 关键词
     * @return 内容包含谓词
     */
    public MessagePredicate contentContains(String keyword) {
        return context -> {
            String content = context.cleanContent();
            return content != null && content.contains(keyword);
        };
    }
    
    /**
     * 创建内容开始于谓词
     * @param prefix 前缀
     * @return 内容开始于谓词
     */
    public MessagePredicate contentStartsWith(String prefix) {
        return context -> {
            String content = context.cleanContent();
            return content != null && content.startsWith(prefix);
        };
    }
    
    // ========== 组合谓词 ==========
    
    /**
     * 好友消息谓词
     * @return 好友消息谓词
     */
    public MessagePredicate friendMessage() {
        return messageType(MessageType.FRIEND);
    }
    
    /**
     * 私聊消息谓词（非群聊）
     * @return 私聊消息谓词
     */
    public MessagePredicate privateMessage() {
        return friendMessage().and(groupChat().negate());
    }
    
    /**
     * 命令消息谓词
     * @param prefix 命令前缀
     * @return 命令消息谓词
     */
    public MessagePredicate commandMessage(String prefix) {
        return friendMessage()
                .and(hasContent())
                .and(commandPrefix(prefix));
    }
    
    /**
     * 普通聊天消息谓词（非命令消息）
     * @param commandPrefix 命令前缀
     * @return 普通聊天消息谓词
     */
    public MessagePredicate chatMessage(String commandPrefix) {
        return friendMessage()
                .and(hasContent())
                .and(commandPrefix(commandPrefix).negate());
    }
    
    /**
     * 群聊@机器人消息谓词
     * @param robotName 机器人名称
     * @return 群聊@机器人消息谓词
     */
    public MessagePredicate groupAtRobotMessage(String robotName) {
        return friendMessage()
                .and(groupChat())
                .and(hasContent())
                .and(contentContains("@" + robotName));
    }
}
