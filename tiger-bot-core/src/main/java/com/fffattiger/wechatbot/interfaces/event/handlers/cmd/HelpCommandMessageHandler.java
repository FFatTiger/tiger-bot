package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;

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
        context.wx().sendText(context.currentChat().chat().name(), help);
    }

    @Override
    public String description() {
        return "/help 查看帮助";
    }
}