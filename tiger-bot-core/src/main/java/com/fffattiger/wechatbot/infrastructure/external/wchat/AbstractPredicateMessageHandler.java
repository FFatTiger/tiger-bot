package com.fffattiger.wechatbot.infrastructure.external.wchat;

/**
 * 基于谓词的消息处理器抽象基类
 * 使用新的谓词模式，将条件判断与处理逻辑分离
 */
public abstract class AbstractPredicateMessageHandler implements MessageHandler {
    
    /**
     * 获取处理条件谓词
     * 子类必须实现此方法来定义处理条件
     * @return 消息谓词
     */
    @Override
    public abstract MessagePredicate getPredicate();
    
    /**
     * 处理消息（不需要条件判断）
     * 子类必须实现此方法来定义具体的处理逻辑
     * @param context 上下文
     * @param chain 责任链传递
     * @return 处理结果
     */
    @Override
    public abstract boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain);
    
    /**
     * 处理微信消息的默认实现
     * 先进行谓词判断，再执行具体处理逻辑
     */
    @Override
    public final boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        MessagePredicate predicate = getPredicate();
        if (predicate != null && predicate.test(context)) {
            return doHandle(context, chain);
        }
        return chain.handle(context);
    }
}
