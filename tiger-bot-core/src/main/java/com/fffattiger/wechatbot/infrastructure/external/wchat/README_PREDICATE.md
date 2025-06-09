# 消息处理器谓词模式使用指南

## 概述

新的谓词模式参考了 Spring Cloud Gateway 的设计理念，将消息处理的条件判断与处理逻辑分离，提供了更灵活、可组合的消息处理方式。

## 核心概念

### 1. MessagePredicate（消息谓词）
谓词是一个函数式接口，用于判断消息是否满足特定条件：

```java
@FunctionalInterface
public interface MessagePredicate {
    boolean test(MessageHandlerContext context);
    
    // 支持组合操作
    default MessagePredicate and(MessagePredicate other);
    default MessagePredicate or(MessagePredicate other);
    default MessagePredicate negate();
}
```

### 2. MessagePredicateFactory（谓词工厂）
提供常用谓词的创建方法和预定义的组合谓词：

```java
@Component
public class MessagePredicateFactory {
    // 基础谓词
    public MessagePredicate messageType(MessageType type);
    public MessagePredicate commandPrefix(String prefix);
    public MessagePredicate hasContent();
    public MessagePredicate groupChat();
    
    // 组合谓词
    public MessagePredicate friendMessage();
    public MessagePredicate commandMessage(String prefix);
    public MessagePredicate chatMessage(String commandPrefix);
}
```

### 3. AbstractPredicateMessageHandler（谓词处理器基类）
基于谓词的消息处理器抽象基类，自动处理谓词判断逻辑：

```java
public abstract class AbstractPredicateMessageHandler implements MessageHandler {
    public abstract MessagePredicate getPredicate();
    public abstract boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain);
}
```

## 使用示例

### 1. 创建简单的命令处理器

```java
@Service
public class MyCommandHandler extends AbstractPredicateMessageHandler {
    
    @Autowired
    private MessagePredicateFactory predicateFactory;
    
    @Override
    public MessagePredicate getPredicate() {
        return predicateFactory.commandMessage("/")
                .and(predicateFactory.contentStartsWith("/hello"));
    }
    
    @Override
    public boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain) {
        context.wx().sendText(context.currentChat().chat().getName(), "Hello!");
        return true;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}
```

### 2. 创建复杂的组合条件处理器

```java
@Service
public class AdminCommandHandler extends AbstractPredicateMessageHandler {
    
    @Autowired
    private MessagePredicateFactory predicateFactory;
    
    @Override
    public MessagePredicate getPredicate() {
        // 只有管理员在私聊中发送的管理命令才处理
        return predicateFactory.friendMessage()
                .and(predicateFactory.groupChat().negate()) // 非群聊
                .and(predicateFactory.commandPrefix("/admin"))
                .and(context -> isAdmin(context.message().sender())); // 自定义条件
    }
    
    @Override
    public boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain) {
        // 处理管理员命令
        return true;
    }
    
    private boolean isAdmin(String sender) {
        // 检查是否为管理员
        return true;
    }
}
```

### 3. 创建自定义谓词

```java
// 自定义谓词类
public class TimeBasedPredicate implements MessagePredicate {
    private final int startHour;
    private final int endHour;
    
    public TimeBasedPredicate(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }
    
    @Override
    public boolean test(MessageHandlerContext context) {
        int currentHour = LocalTime.now().getHour();
        return currentHour >= startHour && currentHour <= endHour;
    }
}

// 在处理器中使用
@Override
public MessagePredicate getPredicate() {
    return predicateFactory.friendMessage()
            .and(new TimeBasedPredicate(9, 18)) // 只在工作时间处理
            .and(predicateFactory.hasContent());
}
```

## 预定义谓词

### 基础谓词
- `messageType(MessageType)` - 消息类型判断
- `commandPrefix(String)` - 命令前缀判断
- `hasContent()` - 是否有内容
- `groupChat()` - 是否为群聊
- `sender(String)` - 发送者判断
- `contentMatches(String)` - 内容正则匹配
- `contentContains(String)` - 内容包含判断
- `contentStartsWith(String)` - 内容开始于判断

### 组合谓词
- `friendMessage()` - 好友消息
- `privateMessage()` - 私聊消息（非群聊）
- `commandMessage(String)` - 命令消息
- `chatMessage(String)` - 普通聊天消息（非命令）
- `groupAtRobotMessage(String)` - 群聊@机器人消息

## 谓词组合操作

```java
// AND 组合
MessagePredicate combined = predicate1.and(predicate2);

// OR 组合
MessagePredicate combined = predicate1.or(predicate2);

// 取反
MessagePredicate negated = predicate.negate();

// 复杂组合
MessagePredicate complex = predicateFactory.friendMessage()
        .and(predicateFactory.hasContent())
        .and(predicateFactory.commandPrefix("/").negate())
        .or(predicateFactory.groupAtRobotMessage("机器人名称"));
```

## 向后兼容性

新的谓词模式与现有的处理器完全兼容。现有处理器可以继续使用原有的 `handle` 方法，新处理器可以选择使用谓词模式。

## 最佳实践

1. **优先使用谓词工厂**：使用 `MessagePredicateFactory` 提供的预定义谓词
2. **合理组合条件**：使用 `and`、`or`、`negate` 组合简单谓词
3. **自定义谓词**：对于复杂的业务逻辑，创建专门的谓词类
4. **性能考虑**：谓词判断应该尽可能轻量级
5. **可读性**：使用有意义的谓词名称和组合方式

## 调试和日志

系统会自动记录注册的处理器及其谓词信息，便于调试：

```
注册消息处理器，共5个:
  1. PredicateGroupMessageHandler (优先级: -10) [谓词: MessageType(FRIEND) && HasContent() && GroupChat()]
  2. PredicateStatusCommandMessageHandler (优先级: 0) [谓词: MessageType(FRIEND) && HasContent() && CommandPrefix('/') && ...]
  3. PredicateAiChatMessageHandler (优先级: 10) [谓词: MessageType(FRIEND) && HasContent() && !CommandPrefix('/')]
```
