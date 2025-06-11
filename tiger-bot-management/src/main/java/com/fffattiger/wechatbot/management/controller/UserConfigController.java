package com.fffattiger.wechatbot.management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.management.application.dto.UserConfigurationDto;
import com.fffattiger.wechatbot.management.application.service.UserManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户配置控制器
 */
@Controller
@RequestMapping("/management/bot-config")
@Slf4j
public class UserConfigController {

    @Autowired
    private UserManagementApplicationService userManagementApplicationService;

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String users(Model model) {
        

        List<UserConfigurationDto> allUsers = userManagementApplicationService.getAllUserConfigurations();
        model.addAttribute("users", allUsers);

        return "management/bot-config/users";
    }

    /**
     * 创建用户
     */
    @PostMapping("/users/create")
    @ResponseBody
    public String createUser(
            @RequestParam String username,
            @RequestParam(required = false) String remark) {

        try {
            UserConfigurationDto dto = UserConfigurationDto.forCreation(username, remark);
            userManagementApplicationService.createUserConfiguration(dto);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * 更新用户
     */
    @PostMapping("/users/{id}/update")
    @ResponseBody
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam(required = false) String remark) {

        try {
            UserConfigurationDto dto = UserConfigurationDto.forCreation(username, remark);
            userManagementApplicationService.updateUserConfiguration(id, dto);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/{id}/delete")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        try {
            userManagementApplicationService.deleteUserConfiguration(id);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }
}
