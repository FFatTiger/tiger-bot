// package com.fffattiger.wechatbot.domain.plugin;

// import org.springframework.data.relational.core.mapping.Table;

// @Table("plugins")
// public class Plugin {

//     // @Id
//     private String pluginId; // 插件的唯一ID，来自plugin.properties，作为主键

//     private String name; // 用户友好的插件名称，可以考虑从描述符中提取或自定义

//     private String version; // 插件版本

//     private String author; // 插件作者

//     private String description; // 插件功能描述

//     private PluginStatus status; // 插件的管理状态（启用/禁用），由管理员控制

//     private String path; // 插件JAR文件的物理路径

//     private LocalDateTime installedAt; // 首次被发现并记录的时间

//     // 构造函数、Getter、Setter 等

//     /**
//      * 启用插件。这是一个业务方法，封装了状态变更的逻辑。
//      */
//     public void enable() {
//         if (this.status == PluginStatus.DISABLED) {
//             this.status = PluginStatus.ENABLED;
//             // 可选：在此处发布一个 PluginEnabledEvent 领域事件
//         }
//     }

//     /**
//      * 禁用插件。
//      */
//     public void disable() {
//         if (this.status == PluginStatus.ENABLED) {
//             this.status = PluginStatus.DISABLED;
//             // 可选：在此处发布一个 PluginDisabledEvent 领域事件
//         }
//     }

//     /**
//      * 判断插件是否处于可运行状态。
//      * PF4J可能已经加载了插件，但从业务上它可能被禁用了。
//      */
//     public boolean isEnabled() {
//         return this.status == PluginStatus.ENABLED;
//     }
// }