package com.fffattiger.wechatbot.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.management.service.ManagementService;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 后台管理控制器
 */
@Controller
@RequestMapping("/management")
@Slf4j
public class ManagementController {

    @Autowired
    private ManagementService managementService;
    
    @Autowired
    private ChatBotProperties chatBotProperties;

    /**
     * 管理首页
     */
    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        log.info("访问管理首页");
        
        // 添加基础信息到模型
        model.addAttribute("robotName", chatBotProperties.getRobotName());
        model.addAttribute("systemStatus", managementService.getSystemStatus());
        model.addAttribute("gatewayStatus", managementService.getGatewayStatus());
        
        return "management/index";
    }

    /**
     * 系统配置页面
     */
    @GetMapping("/config")
    public String config(Model model) {
        log.info("访问系统配置页面");
        
        model.addAttribute("chatBotProperties", chatBotProperties);
        
        return "management/config";
    }

    /**
     * Python网关控制页面
     */
    @GetMapping("/gateway")
    public String gateway(Model model) {
        log.info("访问网关控制页面");
        
        model.addAttribute("gatewayStatus", managementService.getGatewayStatus());
        model.addAttribute("pythonScriptPath", "wx_http_sse_gateway.py");
        
        return "management/gateway";
    }

    /**
     * 监控页面
     */
    @GetMapping("/monitor")
    public String monitor(Model model) {
        log.info("访问监控页面");
        
        model.addAttribute("systemMetrics", managementService.getSystemMetrics());
        
        return "management/monitor";
    }

    /**
     * 启动Python网关
     */
    @PostMapping("/gateway/start")
    @ResponseBody
    public String startGateway() {
        log.info("启动Python网关");
        
        try {
            boolean result = managementService.startPythonGateway();
            return result ? "success" : "failed";
        } catch (Exception e) {
            log.error("启动Python网关失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 停止Python网关
     */
    @PostMapping("/gateway/stop")
    @ResponseBody
    public String stopGateway() {
        log.info("停止Python网关");
        
        try {
            boolean result = managementService.stopPythonGateway();
            return result ? "success" : "failed";
        } catch (Exception e) {
            log.error("停止Python网关失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 获取网关状态
     */
    @GetMapping("/gateway/status")
    @ResponseBody
    public String getGatewayStatus() {
        return managementService.getGatewayStatus();
    }

    /**
     * 重启网关
     */
    @PostMapping("/gateway/restart")
    @ResponseBody
    public String restartGateway() {
        log.info("重启Python网关");
        
        try {
            boolean result = managementService.restartPythonGateway();
            return result ? "success" : "failed";
        } catch (Exception e) {
            log.error("重启Python网关失败", e);
            return "error: " + e.getMessage();
        }
    }
}
