package com.fffattiger.wechatbot.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.plugin.Plugin;
import com.fffattiger.wechatbot.domain.plugin.PluginLoaderService;
import com.fffattiger.wechatbot.domain.plugin.repository.PluginRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 插件应用服务
 * 负责插件的业务逻辑协调
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PluginApplicationService {
    
    private final PluginRepository pluginRepository;
    private final PluginLoaderService pluginLoaderService;

    public PluginWrapper loadPlugin(Path pluginPath) {
        return pluginLoaderService.loadPlugin(pluginPath);
    }

    /**
     * 获取所有插件
     */
    public List<Plugin> getAllPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        pluginRepository.findAll().forEach(plugins::add);
        return plugins;
    }

    /**
     * 根据ID获取插件
     */
    public Plugin getPluginById(String pluginId) {
        return pluginRepository.findByPluginId(pluginId)
                .orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginId));
    }

    /**
     * 保存插件
     */
    public Plugin save(Plugin plugin) {
        plugin.initializeBasicInfo();
        return pluginRepository.save(plugin);
    }

    /**
     * 启用插件
     */
    public void enablePlugin(String pluginId) {
        Plugin plugin = getPluginById(pluginId);
        
        if (!plugin.canBeEnabled()) {
            throw new IllegalStateException("插件不能被启用，当前状态: " + plugin.getStatus());
        }

        try {
            // 1. 领域对象状态变更
            plugin.enable();
            
            // 2. 动态加载插件
            pluginLoaderService.loadPlugin(plugin);
            
            // 3. 持久化状态
            pluginRepository.save(plugin);
            
            log.info("插件启用成功: {}", plugin.getName());
            
        } catch (Exception e) {
            // 回滚状态
            plugin.markAsError();
            pluginRepository.save(plugin);
            
            log.error("插件启用失败: {}", plugin.getName(), e);
            throw new RuntimeException("插件启用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 禁用插件
     */
    public void disablePlugin(String pluginId) {
        Plugin plugin = getPluginById(pluginId);
        
        if (!plugin.canBeDisabled()) {
            throw new IllegalStateException("插件不能被禁用，当前状态: " + plugin.getStatus());
        }

        try {
            // 1. 动态卸载插件
            pluginLoaderService.unloadPlugin(plugin);
            
            // 2. 领域对象状态变更
            plugin.disable();
            
            // 3. 持久化状态
            pluginRepository.save(plugin);
            
            log.info("插件禁用成功: {}", plugin.getName());
            
        } catch (Exception e) {
            log.error("插件禁用失败: {}", plugin.getName(), e);
            throw new RuntimeException("插件禁用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新插件参数
     */
    public void updatePluginParameters(String pluginId, String parameters) {
        Plugin plugin = getPluginById(pluginId);
        
        // 使用聚合根的业务方法
        plugin.updateParameters(parameters);
        
        // 持久化
        pluginRepository.save(plugin);
        
        log.info("插件参数更新成功: {}", plugin.getName());
    }

    /**
     * 删除插件
     */
    public void deletePlugin(String pluginId) {
        Plugin plugin = getPluginById(pluginId);
        
        // 如果插件处于启用状态，先禁用
        if (plugin.getStatus() == PluginStatus.ENABLED) {
            disablePlugin(pluginId);
        }
        
        // 删除插件文件
        try {
            if (plugin.getSourcePath() != null) {
                Files.deleteIfExists(Paths.get(plugin.getSourcePath()));
            }
        } catch (IOException e) {
            log.warn("删除插件文件失败: {}", plugin.getSourcePath(), e);
        }
        
        // 删除数据库记录
        pluginRepository.deleteById(plugin.getId());
        
        log.info("插件删除成功: {}", plugin.getName());
    }

    /**
     * 根据状态获取插件列表
     */
    public List<Plugin> getPluginsByStatus(PluginStatus status) {
        return pluginRepository.findByStatus(status);
    }

    /**
     * 获取启用的插件数量
     */
    public long getEnabledPluginCount() {
        return pluginRepository.findByStatus(PluginStatus.ENABLED).size();
    }

    /**
     * 获取插件统计信息
     */
    public PluginStatistics getStatistics() {
        List<Plugin> allPlugins = getAllPlugins();
        
        long enabledCount = allPlugins.stream().filter(p -> p.getStatus() == PluginStatus.ENABLED).count();
        long disabledCount = allPlugins.stream().filter(p -> p.getStatus() == PluginStatus.DISABLED).count();
        long errorCount = allPlugins.stream().filter(p -> p.getStatus() == PluginStatus.ERROR).count();
        
        return new PluginStatistics(allPlugins.size(), enabledCount, disabledCount, errorCount);
    }

    /**
     * 插件统计信息
     */
    public record PluginStatistics(
        long totalCount,
        long enabledCount,
        long disabledCount,
        long errorCount
    ) {}
}