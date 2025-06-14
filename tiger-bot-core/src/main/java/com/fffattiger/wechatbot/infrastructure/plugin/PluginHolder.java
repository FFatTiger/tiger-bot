package com.fffattiger.wechatbot.infrastructure.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
    
    /**
     * 移除指定插件的所有扩展
     */
    public void removeExtensions(String pluginId) {
        List<MessageHandlerExtension> removed = LOADED_PLUGINS.remove(pluginId);
        if (removed != null) {
            log.info("已移除插件扩展: {}, 扩展数量: {}", pluginId, removed.size());
        } else {
            log.debug("插件扩展不存在或已移除: {}", pluginId);
        }
    }
    
    /**
     * 获取指定插件的扩展
     */
    public List<MessageHandlerExtension> getExtensions(String pluginId) {
        return LOADED_PLUGINS.getOrDefault(pluginId, new ArrayList<>());
    }
    
    /**
     * 获取所有可用命令列表（用于帮助系统）
     */
    public List<String> getAllAvailableCommands() {
        return getAllCommandExtensions().stream()
                .map(CommandMessageHandlerExtension::getCommandName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取命令描述映射（用于帮助文档生成）
     */
    public Map<String, String> getCommandDescriptions() {
        return getAllCommandExtensions().stream()
                .collect(Collectors.toMap(
                    CommandMessageHandlerExtension::getCommandName,
                    CommandMessageHandlerExtension::getDescription,
                    (existing, replacement) -> existing // 处理重复命令名
                ));
    }

    /**
     * 检查命令是否存在
     */
    public boolean hasCommand(String commandName) {
        return getAllCommandExtensions().stream()
                .anyMatch(cmd -> cmd.getCommandName().equals(commandName));
    }

    /**
     * 获取插件统计信息
     */
    public PluginStatistics getStatistics() {
        long totalExtensions = LOADED_PLUGINS.values().stream()
                .mapToLong(List::size)
                .sum();
        
        long commandExtensions = getAllCommandExtensions().size();
        
        return new PluginStatistics(
            LOADED_PLUGINS.size(),
            totalExtensions,
            commandExtensions
        );
    }
    
    /**
     * 插件统计信息
     */
    public record PluginStatistics(
        int loadedPlugins,
        long totalExtensions,
        long commandExtensions
    ) {}
}
