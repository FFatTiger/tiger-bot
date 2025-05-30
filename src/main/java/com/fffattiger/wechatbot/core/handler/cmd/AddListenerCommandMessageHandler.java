package com.fffattiger.wechatbot.core.handler.cmd;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.config.WxChatConfig;
import com.fffattiger.wechatbot.core.WxChat;
import com.fffattiger.wechatbot.core.holder.WxChatHolder;
import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;

@Service
public class AddListenerCommandMessageHandler extends AbstractCommandMessageHandler {

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/增加监听") || command.startsWith("/addlistener") ;
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length < 1) {
            context.wx().sendText(context.currentChat().getChatName(), "命令格式错误，请参考帮助");
            return;
        }
        String chatName = args[0];
        boolean savePic = false;
        boolean saveVoice = false;
        boolean parseLinks = false;
        if (args.length > 1) {
            savePic = Boolean.parseBoolean(args[1]);
        }
        if (args.length > 2) {
            saveVoice = Boolean.parseBoolean(args[2]);
        }
        if (args.length > 3) {
            parseLinks = Boolean.parseBoolean(args[3]);
        }
        context.wx().addListenChat(chatName, savePic, saveVoice, parseLinks);
        WxChatHolder.registerWxChat(new WxChat(WxChatConfig.defaultConfig(chatName), null));
        context.wx().sendText(context.currentChat().getChatName(), "增加监听成功");
    }

    @Override
    public String description() {
        return "/增加监听 /addlistener chatName [savePic] [saveVoice] [parseLinks] 增加监听";
    }
    
}