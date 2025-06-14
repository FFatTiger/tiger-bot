package com.fffattiger.wechatbot.infrastructure.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;

@Component
public class PluginHolder {

    private static final Map<String, List<MessageHandlerExtension>> LOADED_PLUGINS = new HashMap<>();


    public void addExtension(String pluginId, MessageHandlerExtension extension) {
        LOADED_PLUGINS.computeIfAbsent(pluginId, k -> new ArrayList<>()).add(extension);
    }


    public void addExtensions(Map<String, List<MessageHandlerExtension>> extensionsByPluginId) {
        LOADED_PLUGINS.putAll(extensionsByPluginId);
    }

    public List<MessageHandlerExtension> getAllExtensions() {
        return LOADED_PLUGINS.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<CommandMessageHandlerExtension> getAllCommandExtensions() {
        return LOADED_PLUGINS.values().stream()
                .flatMap(List::stream)
                .filter(extension -> extension instanceof CommandMessageHandlerExtension)
                .map(extension -> (CommandMessageHandlerExtension) extension)
                .collect(Collectors.toList());
    }
}
