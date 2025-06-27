package com.fffattiger.wechatbot.application.listener;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.api.handler.MessageHandler;
import com.fffattiger.wechatbot.application.dto.DefaultMessage;
import com.fffattiger.wechatbot.application.handler.DefaultMessageHandlerChain;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.event.MessageReceivedEvent;
import com.fffattiger.wechatbot.infrastructure.external.paste.PasteClient;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;
import com.fffattiger.wechatbot.interfaces.context.DefaultMessageHandlerContext;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReceiveListener {

    private final PluginHolder pluginHolder;

    private final WxAuto wxAuto;

    private final ChatBotProperties chatBotProperties;

    private final PasteClient pasteClient;

    private final RestClient.Builder restClientBuilder;
    
    @EventListener
    public void onMessageReceive(MessageReceivedEvent event) {
        Chat chat = event.getChat();

        List<MessageHandler> handlers = new ArrayList<>(pluginHolder.getAllExtensions());
        DefaultMessageHandlerContext context = new DefaultMessageHandlerContext(wxAuto, new DefaultMessage(event));
        context.setIsGroupChat(chat.isGroupFlag());
        context.setRobotName(chatBotProperties.getRobotName());
        context.setPasteClient(pasteClient);
        context.setRestClientBuilder(restClientBuilder);
        boolean handled = new DefaultMessageHandlerChain(handlers).handle(context);
        log.info("消息处理结果: {}", handled);
    }

    
}
