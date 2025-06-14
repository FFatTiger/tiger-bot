package com.fffattiger.wechatbot.infrastructure.startup;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.application.service.PluginApplicationService;
import com.fffattiger.wechatbot.domain.plugin.Plugin;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;
import com.fffattiger.wechatbot.infrastructure.event.handlers.cmd.CommandMessageHandlerWrapper;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class PluginStartupInitializer implements OrderedInitializer {

    private final PluginHolder pluginHolder;

    private final ChatBotProperties chatBotProperties;

    private final List<MessageHandlerExtension> messageHandlerExtensions;

    private final List<CommandMessageHandlerExtension> commandMessageHandlerExtensions;

    private final PluginApplicationService pluginApplicationService;

    @Override
    public void init() {
        PluginManager pluginManager = new DefaultPluginManager(Paths.get(chatBotProperties.getPluginDir()));
        pluginApplicationService.getAllPlugins().stream().filter(plugin -> plugin.getStatus() == PluginStatus.ENABLED).forEach(plugin -> {
            pluginManager.loadPlugin(Paths.get(plugin.getSourcePath()));
        });
        pluginManager.startPlugins();

        Map<String, List<MessageHandlerExtension>> defaultExtensions = loadDefaultExtensions();
        pluginHolder.addExtensions(defaultExtensions);

        Map<String, List<MessageHandlerExtension>> extensionsByPluginId = pluginManager.getPlugins().stream()
                .peek(p -> log.info("加载插件: {}, 版本: {}, 作者: {}, 描述: {}, 扩展点: {}", p.getPluginId(),
                        p.getDescriptor().getVersion(), p.getDescriptor().getProvider(),
                        p.getDescriptor().getPluginDescription(), p.getDescriptor().getPluginClass()))
                .collect(Collectors.toMap(PluginWrapper::getPluginId,
                        p -> getMessageHandlerExtensions(p, pluginManager)));

        pluginHolder.addExtensions(extensionsByPluginId);
    }

    private Map<String, List<MessageHandlerExtension>> loadDefaultExtensions() {
        List<MessageHandlerExtension> defaultExtensions = new ArrayList<>();
        defaultExtensions.addAll(messageHandlerExtensions);
        defaultExtensions.addAll(commandMessageHandlerExtensions.stream().map(c -> new CommandMessageHandlerWrapper(c, chatBotProperties.getCommandPrefix())).toList());
        
        Map<String, List<MessageHandlerExtension>> defaultExtensionsMap = new HashMap<>();
        defaultExtensionsMap.put("systemPlugin", defaultExtensions);
        return defaultExtensionsMap;
    }

    private List<MessageHandlerExtension> getMessageHandlerExtensions(PluginWrapper pluginWrapper,
                                                                      PluginManager pluginManager) {
        // 获取消息处理扩展
        List<MessageHandlerExtension> extensions = pluginManager.getExtensions(MessageHandlerExtension.class,
                pluginWrapper.getPluginId());

        // 获取命令处理扩展
        List<MessageHandlerExtension> commandExtensions = pluginManager
                .getExtensions(CommandMessageHandlerExtension.class, pluginWrapper.getPluginId())
                .stream().map(c -> new CommandMessageHandlerWrapper(c, chatBotProperties.getCommandPrefix()))
                .collect(Collectors.toList());
        extensions.addAll(commandExtensions);
        return extensions;
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
