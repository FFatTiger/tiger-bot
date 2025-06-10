package com.fffattiger.wechatbot.management.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fffattiger.wechatbot.management.application.service.GatewayManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 管理主页控制器
 */
@Controller
@RequestMapping("/management")
@Slf4j
public class ManagementController {

    @Autowired
    private GatewayManagementApplicationService gatewayManagementApplicationService;

    /**
     * 管理主页
     */
    @GetMapping({"", "/"})
    public String index(Model model) {
        log.info("访问管理主页");
        
        // 获取系统状态信息
        String gatewayStatus = gatewayManagementApplicationService.getGatewayStatus();
        Map<String, Object> systemMetrics = gatewayManagementApplicationService.getSystemMetrics();
        
        model.addAttribute("gatewayStatus", gatewayStatus);
        model.addAttribute("systemMetrics", systemMetrics);
        
        return "management/index";
    }
}
