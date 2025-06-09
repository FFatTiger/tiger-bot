package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.domain.user.repository.UserRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户应用服务
 * 专门负责用户相关的应用逻辑
 */
@Service
@Slf4j
public class UserApplicationService {

    @Resource
    private UserRepository userRepository;

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    /**
     * 创建用户
     */
    public User createUser(String username, String remark) {
        User newUser = new User(null, username, remark);
        
        // 使用领域对象验证
        if (!newUser.isValidUser()) {
            throw new RuntimeException("Invalid user data");
        }
        
        User savedUser = userRepository.save(newUser);
        log.info("创建用户: {}", savedUser);
        return savedUser;
    }

    /**
     * 更新用户
     */
    public User updateUser(Long userId, String username, String remark) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        User updatedUser = new User(userId, username, remark);
        
        // 使用领域对象验证
        if (!updatedUser.isValidUser()) {
            throw new RuntimeException("Invalid user data");
        }
        
        User savedUser = userRepository.save(updatedUser);
        log.info("更新用户: {}", savedUser);
        return savedUser;
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                
        userRepository.deleteById(userId);
        log.info("删除用户: {}", userId);
    }

    /**
     * 根据用户名查找用户
     * 注意：这里使用简单的遍历查找，如果需要高性能可以在Repository中添加专门的查询方法
     */
    public User findByUsername(String username) {
        return getAllUsers().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
