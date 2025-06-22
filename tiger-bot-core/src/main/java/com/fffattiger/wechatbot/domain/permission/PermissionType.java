package com.fffattiger.wechatbot.domain.permission;

/**
 * 权限类型枚举
 */
public enum PermissionType {
    /**
     * 聊天对话权限（基础权限）
     * 控制用户是否可以与bot进行对话
     */
    CHAT_ACCESS,
    
    /**
     * 命令执行权限（具体权限）
     * 控制用户是否可以执行特定命令
     */
    COMMAND_EXECUTION,
    
    /**
     * 管理员权限
     * 控制用户是否可以管理聊天室配置
     */
    ADMIN_ACCESS,
    
    /**
     * 文件上传权限
     * 控制用户是否可以上传文件
     */
    FILE_UPLOAD
} 