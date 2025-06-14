package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;

import jakarta.annotation.Resource;

@Service
public class HelpCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private PluginHolder pluginHolder;
    

    @Override
    public String getCommandName() {
        return "帮助";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        String help = pluginHolder.getAllCommandExtensions().stream().map(CommandMessageHandlerExtension::getDescription).collect(Collectors.joining("\n"));
        context.replyText(context.getMessage().chatName(), help);
    }

    @Override
    public String getDescription() {
        return "/help 查看帮助";
    }


}