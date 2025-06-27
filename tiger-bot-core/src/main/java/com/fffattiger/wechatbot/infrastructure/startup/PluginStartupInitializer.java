package com.fffattiger.wechatbot.infrastructure.startup;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.application.handler.cmd.CommandMessageHandlerWrapper;
import com.fffattiger.wechatbot.application.service.CommandApplicationService;
import com.fffattiger.wechatbot.application.service.PluginApplicationService;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.CommandSource;
import com.fffattiger.wechatbot.domain.command.CommandSourceType;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.permission.service.PermissionDomainService;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginLoaderService;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class PluginStartupInitializer implements Initializer.OrderedInitializer {

    private final PluginHolder pluginHolder;

    private final ChatBotProperties chatBotProperties;

    private final List<MessageHandlerExtension> messageHandlerExtensions;

    private final List<CommandMessageHandlerExtension> commandMessageHandlerExtensions;

    private final CommandApplicationService commandApplicationService;

    private final PluginApplicationService pluginApplicationService;

    private final CommandRepository commandRepository;

    private final PermissionDomainService permissionDomainService;

    private final PluginManager pluginManager;

    private final PluginLoaderService pluginLoaderService;

    @Override
    public void init() {
        loadDefaultExtensions();
        loadImportedPlugins();
    }

    private void loadImportedPlugins() {
        pluginApplicationService.getAllPlugins()
                .stream()
                .filter(plugin -> plugin.getStatus() == PluginStatus.ENABLED)
                .forEach(plugin -> {
                    try {
                        pluginManager.loadPlugin(Paths.get(plugin.getSourcePath()));
                    } catch (Exception e) {
                        log.error("启动时加载插件失败: {}", plugin.getName(), e);
                    }
                });

        pluginManager.startPlugins();

        Map<String, List<MessageHandlerExtension>> extensionsByPluginId = pluginManager.getPlugins().stream()
                .peek(p -> log.info("加载插件: {}, 版本: {}, 作者: {}, 描述: {}, 扩展点: {}", p.getPluginId(),
                        p.getDescriptor().getVersion(), p.getDescriptor().getProvider(),
                        p.getDescriptor().getPluginDescription(), p.getDescriptor().getPluginClass()))
                .collect(Collectors.toMap(
                        PluginWrapper::getPluginId,
                        p -> pluginLoaderService.extractAndRegisterExtensions(p.getPluginId()))
                );

        pluginHolder.addExtensions(extensionsByPluginId);
    }

    private void loadDefaultExtensions() {
        List<MessageHandlerExtension> defaultExtensions = new ArrayList<>(messageHandlerExtensions);

        List<CommandMessageHandlerWrapper> commandWrappers = commandMessageHandlerExtensions.stream()
                .map(c -> new CommandMessageHandlerWrapper(c, commandRepository, permissionDomainService, chatBotProperties.getCommandPrefix()))
                .toList();

        defaultExtensions.addAll(commandWrappers);

        commandWrappers.forEach(wrapper -> {
            CommandMessageHandlerExtension cmdExt = wrapper.getDelegate();
            commandApplicationService.registerCommand(new Command(
                    cmdExt.getCommandName(),
                    cmdExt.getDescription(),
                    null,
                    CommandSource.ofSystem(cmdExt.getCommandName())
            ));
        });

        Map<String, List<MessageHandlerExtension>> defaultExtensionsMap = new HashMap<>();
        defaultExtensionsMap.put("systemPlugin", defaultExtensions);
        pluginHolder.addExtensions(defaultExtensionsMap);
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
