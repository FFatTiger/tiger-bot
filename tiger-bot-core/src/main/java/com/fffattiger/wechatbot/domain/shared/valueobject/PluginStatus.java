package com.fffattiger.wechatbot.domain.shared.valueobject;

public enum PluginStatus {
    /**
     * 插件已启用
     */
    ENABLED,

    /**
     * 插件已被管理员禁用
     */
    DISABLED,

    /**
     * 插件加载时发生错误或存在问题。
     */
    ERROR
}