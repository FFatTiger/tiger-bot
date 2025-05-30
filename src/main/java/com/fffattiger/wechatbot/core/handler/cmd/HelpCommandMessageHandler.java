package com.fffattiger.wechatbot.core.handler.cmd;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Service
public class HelpCommandMessageHandler extends AbstractCommandMessageHandler {

    @Resource
    private List<AbstractCommandMessageHandler> messageHandlers;
    
    @PostConstruct
    public void init() {
        messageHandlers.add(this);
    }

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/help") || command.startsWith("/h") || command.startsWith("/?") || command.startsWith("/帮助") ;
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        String help = messageHandlers.stream().map(AbstractCommandMessageHandler::description).collect(Collectors.joining("\n"));
        context.wx().sendText(context.currentChat().getChatName(), help);
    }

    @Override
    public String description() {
        return "/help 查看帮助";
    }
}