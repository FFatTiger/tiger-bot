package com.fffattiger.wechatbot.domain.permission.service;

import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fffattiger.wechatbot.domain.permission.Permission;
import com.fffattiger.wechatbot.domain.permission.PermissionType;
import com.fffattiger.wechatbot.domain.permission.repository.PermissionRepository;

import jakarta.annotation.Resource;

/**
 * 权限领域服务
 */
@Service
public class PermissionDomainService {

    @Resource
    private PermissionRepository permissionRepository;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 检查用户是否可以在聊天室中对话（基础权限）
     */
    public boolean canAccessChat(Long userId, Long chatGroupId) {
        List<Permission> permissions = permissionRepository
            .findByTypeAndContextId(PermissionType.CHAT_ACCESS, chatGroupId.toString());
            
        // 如果没有任何聊天权限配置，默认允许所有人对话
        if (permissions.isEmpty()) {
            return true;
        }
        
        return permissions.stream()
            .anyMatch(p -> p.allows(
                userId != null ? userId.toString() : null, 
                null, // 对话权限不需要resourceId
                chatGroupId.toString()
            ));
    }

    /**
     * 检查用户是否可以执行特定命令（具体权限）
     * 需要同时满足：1.有对话权限 2.有命令执行权限
     */
    public boolean canExecuteCommand(Long userId, Long commandId, Long chatGroupId) {
        // 1. 首先检查基础对话权限
        if (!canAccessChat(userId, chatGroupId)) {
            return false;
        }
        
        // 2. 检查具体命令执行权限
        List<Permission> commandPermissions = permissionRepository
            .findByTypeAndResourceIdAndContextId(
                PermissionType.COMMAND_EXECUTION, 
                commandId.toString(), 
                chatGroupId.toString()
            );
            
        // 如果没有配置命令权限，默认有对话权限的用户都可以执行
        if (commandPermissions.isEmpty()) {
            return true;
        }
            
        return commandPermissions.stream()
            .anyMatch(p -> p.allows(
                userId != null ? userId.toString() : null, 
                commandId.toString(), 
                chatGroupId.toString()
            ));
    }

    /**
     * 检查用户是否为聊天室管理员
     */
    public boolean isAdmin(Long userId, Long chatGroupId) {
        if (userId == null) {
            return false;
        }
        
        List<Permission> adminPermissions = permissionRepository
            .findByTypeAndContextId(PermissionType.ADMIN_ACCESS, chatGroupId.toString());
            
        return adminPermissions.stream()
            .anyMatch(p -> p.allows(
                userId.toString(), 
                null, 
                chatGroupId.toString()
            ));
    }

    /**
     * 授予聊天对话权限
     */
    @Transactional
    public void grantChatAccess(Long chatGroupId, Long userId) {
        Permission permission = Permission.createChatAccessPermission(chatGroupId, userId);
        permissionRepository.save(permission);
        
        // 发布权限变更事件
        // eventPublisher.publishEvent(new PermissionGrantedEvent(permission));
    }

    /**
     * 授予命令执行权限
     */
    @Transactional
    public void grantCommandPermission(Long chatGroupId, Long commandId, Long userId) {
        Permission permission = Permission.createCommandPermission(chatGroupId, commandId, userId);
        permissionRepository.save(permission);
        
        // 发布权限变更事件
        // eventPublisher.publishEvent(new PermissionGrantedEvent(permission));
    }

    /**
     * 授予管理员权限
     */
    @Transactional
    public void grantAdminPermission(Long chatGroupId, Long userId) {
        Permission permission = Permission.createAdminPermission(chatGroupId, userId);
        permissionRepository.save(permission);
        
        // 发布权限变更事件
        // eventPublisher.publishEvent(new PermissionGrantedEvent(permission));
    }

    /**
     * 设置聊天室为私有模式（只有指定用户可对话）
     */
    @Transactional
    public void enablePrivateChat(Long chatGroupId, List<Long> allowedUserIds) {
        // 先清除现有的对话权限
        permissionRepository.deleteByTypeAndContextId(PermissionType.CHAT_ACCESS, chatGroupId.toString());
        
        // 为指定用户授予对话权限
        for (Long userId : allowedUserIds) {
            grantChatAccess(chatGroupId, userId);
        }
        
        // 发布聊天室隐私设置变更事件
        // eventPublisher.publishEvent(new ChatPrivacyChangedEvent(chatGroupId, true, allowedUserIds));
    }

    /**
     * 设置聊天室为公开模式（所有人可对话）
     */
    @Transactional
    public void enablePublicChat(Long chatGroupId) {
        // 清除所有对话权限配置，回到默认状态
        permissionRepository.deleteByTypeAndContextId(PermissionType.CHAT_ACCESS, chatGroupId.toString());
        
        // 发布聊天室隐私设置变更事件
        // eventPublisher.publishEvent(new ChatPrivacyChangedEvent(chatGroupId, false, Collections.emptyList()));
    }

    /**
     * 撤销用户的特定权限
     */
    @Transactional
    public void revokePermission(Long chatGroupId, PermissionType type, Long userId, Long resourceId) {
        String resourceIdStr = resourceId != null ? resourceId.toString() : null;
        String userIdStr = userId != null ? userId.toString() : null;
        
        List<Permission> permissions = permissionRepository
            .findByTypeAndResourceIdAndContextId(type, resourceIdStr, chatGroupId.toString());
            
        permissions.stream()
            .filter(p -> p.allows(userIdStr, resourceIdStr, chatGroupId.toString()))
            .forEach(Permission::revoke);
            
        permissionRepository.saveAll(permissions);
    }

    /**
     * 清除聊天室的所有权限配置
     */
    @Transactional
    public void clearAllPermissions(Long chatGroupId) {
        permissionRepository.deleteByContextId(chatGroupId.toString());
    }

    /**
     * 获取用户在聊天室中的所有权限
     */
    public List<Permission> getUserPermissions(Long userId, Long chatGroupId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return permissionRepository.findUserPermissionsInContext(
            userId.toString(), 
            chatGroupId.toString()
        );
    }

    /**
     * 检查聊天室是否为私有模式
     */
    public boolean isPrivateChat(Long chatGroupId) {
        List<Permission> chatAccessPermissions = permissionRepository
            .findByTypeAndContextId(PermissionType.CHAT_ACCESS, chatGroupId.toString());
        
        return !chatAccessPermissions.isEmpty();
    }
} 