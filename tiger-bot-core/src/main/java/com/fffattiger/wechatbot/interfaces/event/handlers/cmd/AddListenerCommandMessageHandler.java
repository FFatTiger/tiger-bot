package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;
// package com.fffattiger.wechatbot.core.handler.cmd;

// import org.springframework.stereotype.Service;

// import com.fffattiger.wechatbot.properties.WxChatConfig;
// import com.fffattiger.wechatbot.repository.ListenerRepository;
// import com.fffattiger.wechatbot.core.WxChat;
// import com.fffattiger.wechatbot.core.holder.WxChatHolder;
// import com.fffattiger.wechatbot.entity.ChatEntity;
// import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;

// import jakarta.annotation.Resource;

// @Service
// public class AddListenerCommandMessageHandler extends AbstractCommandMessageHandler {

//     @Resource
//     private ListenerRepository listenerRepository;

//     @Override
//     public boolean canHandle(String command) {
//         return command.startsWith("/增加监听") || command.startsWith("/addlistener") ;
//     }

//     @Override
//     public void doHandle(String command, String[] args, MessageHandlerContext context) {
//         if (args.length < 1) {
//             context.wx().sendText(context.currentChat().chat().name(), "命令格式错误，请参考帮助");
//             return;
//         }
//         String chatName = args[0];
//         boolean savePic = false;
//         boolean saveVoice = false;
//         boolean parseLinks = false;
//         if (args.length > 1) {
//             savePic = Boolean.parseBoolean(args[1]);
//         }
//         if (args.length > 2) {
//             saveVoice = Boolean.parseBoolean(args[2]);
//         }
//         if (args.length > 3) {
//             parseLinks = Boolean.parseBoolean(args[3]);
//         }

//         ChatEntity chatEntity = new ChatEntity(null, chatName, true);
//         ListenerEntity listenerEntity = new ListenerEntity(null, chatEntity, null, true, true, savePic, saveVoice, parseLinks, null);
//         context.wx().addListenChat(chatName, savePic, saveVoice, parseLinks);
//         context.wx().sendText(context.currentChat().getChatName(), "增加监听成功");
//     }

//     @Override
//     public String description() {
//         return "/增加监听 chatName 增加监听";
//     }
    
// }