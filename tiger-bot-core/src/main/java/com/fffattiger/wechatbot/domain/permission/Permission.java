package com.fffattiger.wechatbot.domain.permission;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.util.Assert;

import com.fffattiger.wechatbot.domain.common.AggregateRoot;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * 权限聚合根 - 统一管理所有权限
 */
@Entity
@Table(name = "permissions")
@Getter
public class Permission extends AggregateRoot {

    @Enumerated(EnumType.STRING)
    private PermissionType type;
    
    /**
     * 资源ID：commandId、fileId等（某些权限可为null）
     */
    private String resourceId;
    
    /**
     * 主体ID：userId（可为null表示全局权限）
     */
    private String subjectId;
    
    /**
     * 上下文ID：chatId
     */
    private String contextId;
    
    private boolean granted;
    
    /**
     * 可选的过期时间
     */
    private LocalDateTime expiresAt;

    // 构造函数
    protected Permission() {}

    public Permission(PermissionType type, String resourceId, String subjectId, String contextId) {
        Assert.notNull(type, "Permission type cannot be null");
        Assert.hasText(contextId, "Context ID cannot be empty");
        
        this.type = type;
        this.resourceId = resourceId;
        this.subjectId = subjectId;
        this.contextId = contextId;
        this.granted = true;
    }

    // 业务方法
    public boolean allows(String subjectId, String resourceId, String contextId) {
        if (!granted || isExpired()) {
            return false;
        }
        return matchesSubject(subjectId) && 
               matchesResource(resourceId) && 
               matchesContext(contextId);
    }

    private boolean matchesSubject(String subjectId) {
        // null表示全局权限，匹配所有用户
        if (this.subjectId == null) {
            return true;
        }
        return Objects.equals(this.subjectId, subjectId);
    }

    private boolean matchesResource(String resourceId) {
        // null表示不需要特定资源，如对话权限
        if (this.resourceId == null) {
            return true;
        }
        return Objects.equals(this.resourceId, resourceId);
    }

    private boolean matchesContext(String contextId) {
        return Objects.equals(this.contextId, contextId);
    }

    private boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // 工厂方法 - 创建聊天对话权限
    public static Permission createChatAccessPermission(Long chatGroupId, Long userId) {
        return new Permission(
            PermissionType.CHAT_ACCESS,
            null, // 对话权限不需要specific resource
            userId != null ? userId.toString() : null,
            chatGroupId.toString()
        );
    }

    // 工厂方法 - 创建命令执行权限
    public static Permission createCommandPermission(Long chatGroupId, Long commandId, Long userId) {
        return new Permission(
            PermissionType.COMMAND_EXECUTION,
            commandId.toString(),
            userId != null ? userId.toString() : null,
            chatGroupId.toString()
        );
    }

    // 工厂方法 - 创建管理员权限
    public static Permission createAdminPermission(Long chatGroupId, Long userId) {
        return new Permission(
            PermissionType.ADMIN_ACCESS,
            null,
            userId.toString(),
            chatGroupId.toString()
        );
    }

    // 权限层次检查
    public boolean isBasicPermission() {
        return type == PermissionType.CHAT_ACCESS || type == PermissionType.ADMIN_ACCESS;
    }

    public boolean isSpecificPermission() {
        return type == PermissionType.COMMAND_EXECUTION || type == PermissionType.FILE_UPLOAD;
    }

    // 权限管理
    public void revoke() {
        this.granted = false;
    }

    public void grant() {
        this.granted = true;
    }

    public void setExpiration(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isGlobalPermission() {
        return subjectId == null;
    }
} 