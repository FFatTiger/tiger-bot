package com.fffattiger.wechatbot.management.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.management.application.service.GatewayManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关控制器
 */
@Controller
@RequestMapping("/management/gateway")
@Slf4j
public class GatewayController {

    @Autowired
    private GatewayManagementApplicationService gatewayManagementApplicationService;

    /**
     * 网关控制页面
     */
    @GetMapping("")
    public String gateway(Model model) {
        log.info("访问网关控制页面");
        
        String gatewayStatus = gatewayManagementApplicationService.getGatewayStatus();
        Map<String, Object> systemMetrics = gatewayManagementApplicationService.getSystemMetrics();
        
        model.addAttribute("gatewayStatus", gatewayStatus);
        model.addAttribute("systemMetrics", systemMetrics);
        model.addAttribute("pythonScriptPath", "wx_http_sse_gateway.py");
        
        return "management/gateway";
    }

    /**
     * 获取网关状态
     */
    @GetMapping("/status")
    @ResponseBody
    public String getStatus() {
        return gatewayManagementApplicationService.getGatewayStatus();
    }

    /**
     * 获取系统指标
     */
    @GetMapping("/metrics")
    @ResponseBody
    public Map<String, Object> getMetrics() {
        return gatewayManagementApplicationService.getSystemMetrics();
    }

    /**
     * 启动网关
     */
    @PostMapping("/start")
    @ResponseBody
    public String startGateway() {
        try {
            boolean success = gatewayManagementApplicationService.startGateway();
            return success ? "success" : "failed";
        } catch (Exception e) {
            log.error("启动网关失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 停止网关
     */
    @PostMapping("/stop")
    @ResponseBody
    public String stopGateway() {
        try {
            boolean success = gatewayManagementApplicationService.stopGateway();
            return success ? "success" : "failed";
        } catch (Exception e) {
            log.error("停止网关失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 重启网关
     */
    @PostMapping("/restart")
    @ResponseBody
    public String restartGateway() {
        try {
            boolean success = gatewayManagementApplicationService.restartGateway();
            return success ? "success" : "failed";
        } catch (Exception e) {
            log.error("重启网关失败", e);
            return "error: " + e.getMessage();
        }
    }
}
