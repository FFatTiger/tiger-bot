package com.fffattiger.wechatbot.interfaces.event.handlers;
// package com.tiger.wechatbot.core.handler;

// import java.util.List;

// import org.springframework.stereotype.Service;
// import org.springframework.util.CollectionUtils;

// import com.tiger.wechatbot.core.ChatHistoryCollector;
// import com.tiger.wechatbot.wxauto.MessageHandler;
// import com.tiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;
// import com.tiger.wechatbot.wxauto.MessageHandlerContext;
// import com.tiger.wechatbot.wxauto.MessageType;

// import jakarta.annotation.Resource;

// import com.tiger.wechatbot.wxauto.MessageHandlerChain;

// @Service
// public class RecallMessageHandler implements MessageHandler {

//     @Resource
//     private ChatHistoryCollector chatHistoryCollector;

//     @Override
//     public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
//         if (context.message().type() != MessageType.RECALL) {
//             return chain.handle(context);
//         }
//         String chatName = context.currentChat().getChatName();
        
//         Message message = context.message();
//         // 获取撤回消息的内容
//         String sender = message.content().split("撤回了一条消息")[0].trim().replaceAll("\"", "");


//         List<Message> messages = chatHistoryCollector.query(chatName, sender);
//         Message lastMessage = CollectionUtils.lastElement(messages);

//         if (lastMessage == null) {
//             return true;
//         }

//         context.wx().sendText(chatName, "小样，还敢撤回 \n 撤回内容：【" + lastMessage.content() + "】");
//         return true;
//     }

//     @Override
//     public int getOrder() {
//         return -20;
//     }
// }