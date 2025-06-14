package com.fffattiger.wechatbot.domain.plugin;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginLoadType;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginType;

import lombok.Data;

@Table("plugins")
@Data
public class Plugin {

    @Id
    private Long id;

    private String pluginId;

    private String name; 

    private String version; 

    private String author; 

    private String description; 

    private PluginStatus status = PluginStatus.DISABLED; 

    private String sourceUrl;
    
    private String sourcePath;

    private Double size;

    private String parameters;

    private PluginLoadType loadType; 
    
    private PluginType pluginType;

    private LocalDateTime installedAt; 

    private LocalDateTime updatedAt; 

    private LocalDateTime lastLoadedAt; 

    private LocalDateTime lastUnloadedAt; 
    
    /**
     * 启用插件
     * 业务规则：只有DISABLED或ERROR状态的插件才能启用
     */
    public void enable() {
        if (this.status == PluginStatus.ENABLED) {
            throw new IllegalStateException("插件已处于启用状态");
        }
        this.status = PluginStatus.ENABLED;
        this.lastLoadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用插件
     * 业务规则：只有ENABLED状态的插件才能禁用
     */
    public void disable() {
        if (this.status == PluginStatus.DISABLED) {
            throw new IllegalStateException("插件已处于禁用状态");
        }
        this.status = PluginStatus.DISABLED;
        this.lastUnloadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记插件为错误状态
     */
    public void markAsError() {
        this.status = PluginStatus.ERROR;
        this.lastUnloadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新插件参数
     * 业务规则：验证JSON格式
     */
    public void updateParameters(String newParameters) {
        if (newParameters != null && !newParameters.trim().isEmpty() && !isValidJson(newParameters)) {
            throw new IllegalArgumentException("插件参数格式无效，必须是有效的JSON");
        }
        this.parameters = newParameters;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否可以启用
     */
    public boolean canBeEnabled() {
        return this.status == PluginStatus.DISABLED || this.status == PluginStatus.ERROR;
    }

    /**
     * 检查是否可以禁用
     */
    public boolean canBeDisabled() {
        return this.status == PluginStatus.ENABLED;
    }

    /**
     * 检查是否为命令插件
     */
    public boolean isCommandPlugin() {
        return this.pluginType == PluginType.COMMAND_HANDLER;
    }
    
    /**
     * 检查是否为消息处理插件
     */
    public boolean isMessageHandlerPlugin() {
        return this.pluginType == PluginType.MESSAGE_HANDLER;
    }

    /**
     * 验证JSON格式
     */
    private boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 初始化插件基本信息
     */
    public void initializeBasicInfo() {
        LocalDateTime now = LocalDateTime.now();
        if (this.installedAt == null) {
            this.installedAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = PluginStatus.DISABLED;
        }
    }
}