package com.fffattiger.wechatbot.management.application.dto;

import java.time.LocalDateTime;

import com.fffattiger.wechatbot.domain.shared.valueobject.PluginLoadType;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;

/**
 * 插件管理DTO
 */
public record PluginManagementDto(
    String pluginId,
    String name,
    String version,
    String author,
    String description,
    PluginStatus status,
    PluginLoadType loadType,
    String parameters,
    Double size,
    LocalDateTime installedAt,
    LocalDateTime updatedAt,
    boolean isCommandPlugin
) {
    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        return switch (status) {
            case ENABLED -> "已启用";
            case DISABLED -> "已禁用";
            case ERROR -> "错误";
        };
    }

    /**
     * 获取状态样式类
     */
    public String getStatusClass() {
        return switch (status) {
            case ENABLED -> "bg-success";
            case DISABLED -> "bg-secondary";
            case ERROR -> "bg-danger";
        };
    }

    /**
     * 获取加载类型显示文本
     */
    public String getLoadTypeText() {
        return switch (loadType) {
            case LOCAL -> "本地插件";
            case REMOTE -> "远程插件";
            case SYSTEM -> "系统插件";
        };
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedSize() {
        if (size == null) return "未知";
        return String.format("%.2f MB", size);
    }

    /**
     * 检查是否可以启用
     */
    public boolean canBeEnabled() {
        return status == PluginStatus.DISABLED || status == PluginStatus.ERROR;
    }

    /**
     * 检查是否可以禁用
     */
    public boolean canBeDisabled() {
        return status == PluginStatus.ENABLED;
    }
}