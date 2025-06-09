// package com.fffattiger.wechatbot.infrastructure.config;

// import java.util.List;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.annotation.AnnotationAwareOrderComparator;

// import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;

// import lombok.extern.slf4j.Slf4j;

// /**
//  * 消息处理器配置类
//  * 负责注册和管理消息处理器
//  */
// @Configuration
// @Slf4j
// public class MessageHandlerConfiguration {

//     /**
//      * 注册所有消息处理器并按优先级排序
//      * Spring会自动收集所有MessageHandler类型的Bean
//      */
//     @Bean
//     public List<MessageHandler> messageHandlers(List<MessageHandler> handlers) {
//         // 按照Order注解或getOrder()方法排序
//         handlers.sort(AnnotationAwareOrderComparator.INSTANCE);
        
//         log.info("注册消息处理器，共{}个:", handlers.size());
//         for (int i = 0; i < handlers.size(); i++) {
//             MessageHandler handler = handlers.get(i);
//             String predicateInfo = "";
            
//             // 如果是基于谓词的处理器，显示谓词信息
//             if (handler.getPredicate() != null) {
//                 predicateInfo = " [谓词: " + handler.getPredicate().toString() + "]";
//             }
            
//             log.info("  {}. {} (优先级: {}){}", 
//                 i + 1, 
//                 handler.getClass().getSimpleName(), 
//                 handler.getOrder(),
//                 predicateInfo);
//         }
        
//         return handlers;
//     }
// }
