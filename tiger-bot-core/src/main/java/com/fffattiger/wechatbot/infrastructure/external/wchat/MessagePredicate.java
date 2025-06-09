package com.fffattiger.wechatbot.infrastructure.external.wchat;

/**
 * 消息谓词接口，用于判断消息是否满足特定条件
 * 类似于 Spring Cloud Gateway 的谓词模式
 */
@FunctionalInterface
public interface MessagePredicate {
    
    /**
     * 测试消息是否满足条件
     * @param context 消息处理上下文
     * @return true 如果满足条件，false 否则
     */
    boolean test(MessageHandlerContext context);
    
    /**
     * 与另一个谓词进行 AND 组合
     * @param other 另一个谓词
     * @return 组合后的谓词
     */
    default MessagePredicate and(MessagePredicate other) {
        return context -> this.test(context) && other.test(context);
    }
    
    /**
     * 与另一个谓词进行 OR 组合
     * @param other 另一个谓词
     * @return 组合后的谓词
     */
    default MessagePredicate or(MessagePredicate other) {
        return context -> this.test(context) || other.test(context);
    }
    
    /**
     * 对当前谓词进行取反
     * @return 取反后的谓词
     */
    default MessagePredicate negate() {
        return context -> !this.test(context);
    }
    
    /**
     * 总是返回 true 的谓词
     * @return 总是为真的谓词
     */
    static MessagePredicate alwaysTrue() {
        return context -> true;
    }
    
    /**
     * 总是返回 false 的谓词
     * @return 总是为假的谓词
     */
    static MessagePredicate alwaysFalse() {
        return context -> false;
    }
}
