package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.UserApplicationService;
import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.management.application.dto.UserConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户管理应用服务
 * 负责用户配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class UserManagementApplicationService {

    @Autowired
    private UserApplicationService coreUserApplicationService;

    /**
     * 获取所有用户配置
     */
    public List<UserConfigurationDto> getAllUserConfigurations() {
        List<User> users = coreUserApplicationService.getAllUsers();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建用户配置
     */
    public void createUserConfiguration(UserConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("用户配置无效");
        }

        

        // 调用核心模块的应用服务
        coreUserApplicationService.createUser(
            dto.username(),
            dto.remark()
        );
    }

    /**
     * 更新用户配置
     */
    public void updateUserConfiguration(Long id, UserConfigurationDto dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }

        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("用户配置无效");
        }

        

        // 调用核心模块的应用服务
        coreUserApplicationService.updateUser(
            id,
            dto.username(),
            dto.remark()
        );
    }

    /**
     * 删除用户配置
     */
    public void deleteUserConfiguration(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }

        

        // 调用核心模块的应用服务
        coreUserApplicationService.deleteUser(id);
    }

    /**
     * 转换为DTO
     */
    private UserConfigurationDto convertToDto(User user) {
        return new UserConfigurationDto(
            user.getId(),
            user.getUsername(),
            user.getRemark()
        );
    }
}
