package com.fffattiger.wechatbot.domain.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.infrastructure.event.handlers.cmd.CommandMessageHandlerWrapper;
import com.fffattiger.wechatbot.infrastructure.plugin.PluginHolder;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 插件加载服务
 * 负责插件的动态加载和卸载
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PluginLoaderService {
    
    private final PluginManager pluginManager;
    private final PluginHolder pluginHolder;
    private final ChatBotProperties chatBotProperties;

    public PluginWrapper loadPlugin(Path pluginPath) {
        String pluginId = pluginManager.loadPlugin(pluginPath);
        if (pluginId == null) {
            throw new RuntimeException("插件读取失败: " + pluginPath);
        }
        return pluginManager.getPlugin(pluginId);
    }

    /**
     * 加载插件
     */
    public void loadPlugin(Plugin plugin) {
        try {
            String pluginPath = plugin.getSourcePath();
            if (pluginPath == null || pluginPath.trim().isEmpty()) {
                throw new RuntimeException("插件路径为空: " + plugin.getName());
            }
            
            // 1. 加载插件JAR
            String pluginId = pluginManager.loadPlugin(Paths.get(pluginPath));
            if (pluginId == null) {
                throw new RuntimeException("插件加载失败: " + plugin.getName());
            }
            
            // 2. 启动插件
            PluginState state = pluginManager.startPlugin(pluginId);
            if (state != PluginState.STARTED) {
                throw new RuntimeException("插件启动失败: " + plugin.getName() + ", 状态: " + state);
            }
            
            // 3. 提取插件扩展并注册
            List<MessageHandlerExtension> extensions = extractExtensions(pluginId);
            Map<String, List<MessageHandlerExtension>> extensionsMap = new HashMap<>();
            extensionsMap.put(pluginId, extensions);
            pluginHolder.addExtensions(extensionsMap);
            
            log.info("插件加载成功: {} ({}), 扩展数量: {}", plugin.getName(), pluginId, extensions.size());
            
        } catch (Exception e) {
            log.error("插件加载失败: {}", plugin.getName(), e);
            throw new RuntimeException("插件加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 卸载插件
     */
    public void unloadPlugin(Plugin plugin) {
        try {
            String pluginId = plugin.getPluginId();
            
            // 1. 从PluginHolder移除扩展
            pluginHolder.removeExtensions(pluginId);
            
            // 2. 停止插件
            PluginState state = pluginManager.stopPlugin(pluginId);
            log.debug("插件停止状态: {} -> {}", pluginId, state);
            
            // 3. 卸载插件
            boolean unloaded = pluginManager.unloadPlugin(pluginId);
            
            if (!unloaded) {
                log.warn("插件卸载失败: {}", plugin.getName());
            } else {
                log.info("插件卸载成功: {}", plugin.getName());
            }
            
        } catch (Exception e) {
            log.error("插件卸载失败: {}", plugin.getName(), e);
            throw new RuntimeException("插件卸载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查插件是否可以加载
     */
    public boolean canLoadPlugin(String jarPath) {
        try {
            if (jarPath == null || jarPath.trim().isEmpty()) {
                return false;
            }
            
            Path path = Paths.get(jarPath);
            return path.toFile().exists() && jarPath.endsWith(".jar");
        } catch (Exception e) {
            log.debug("检查插件是否可加载失败: {}, 错误: {}", jarPath, e.getMessage());
            return false;
        }
    }

    /**
     * 提取插件扩展
     */
    private List<MessageHandlerExtension> extractExtensions(String pluginId) {
        List<MessageHandlerExtension> extensions = new ArrayList<>();
        
        try {
            // 提取消息处理扩展
            List<MessageHandlerExtension> messageExtensions = 
                    pluginManager.getExtensions(MessageHandlerExtension.class, pluginId);
            extensions.addAll(messageExtensions);
            
            // 提取命令扩展
            List<CommandMessageHandlerExtension> commandExtensions = 
                    pluginManager.getExtensions(CommandMessageHandlerExtension.class, pluginId);
            
            // 包装命令扩展
            for (CommandMessageHandlerExtension cmdExt : commandExtensions) {
                CommandMessageHandlerWrapper wrapper = new CommandMessageHandlerWrapper(
                        cmdExt, chatBotProperties.getCommandPrefix());
                extensions.add(wrapper);
            }
            
            log.debug("插件 {} 提取扩展: 消息处理={}, 命令处理={}", 
                    pluginId, messageExtensions.size(), commandExtensions.size());
            
        } catch (Exception e) {
            log.error("提取插件扩展失败: {}", pluginId, e);
        }
        
        return extensions;
    }
} 