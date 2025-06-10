package com.fffattiger.wechatbot.management.application.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.management.domain.service.GatewayDomainService;
import com.fffattiger.wechatbot.management.infrastructure.gateway.PythonGatewayManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 网关管理应用服务
 * 负责Python网关的管理逻辑
 */
@Service
@Slf4j
public class GatewayManagementApplicationService {

    @Autowired
    private PythonGatewayManager pythonGatewayManager;
    
    @Autowired
    private GatewayDomainService gatewayDomainService;

    /**
     * 获取网关状态
     */
    public String getGatewayStatus() {
        return pythonGatewayManager.getStatus();
    }

    /**
     * 获取系统指标
     */
    public Map<String, Object> getSystemMetrics() {
        return pythonGatewayManager.getSystemMetrics();
    }

    /**
     * 启动网关
     */
    public boolean startGateway() {
        log.info("启动Python网关");
        
        if (!gatewayDomainService.canStartGateway()) {
            log.warn("当前状态不允许启动网关");
            return false;
        }
        
        return pythonGatewayManager.start();
    }

    /**
     * 停止网关
     */
    public boolean stopGateway() {
        log.info("停止Python网关");
        
        if (!gatewayDomainService.canStopGateway()) {
            log.warn("当前状态不允许停止网关");
            return false;
        }
        
        return pythonGatewayManager.stop();
    }

    /**
     * 重启网关
     */
    public boolean restartGateway() {
        log.info("重启Python网关");
        
        if (!gatewayDomainService.canRestartGateway()) {
            log.warn("当前状态不允许重启网关");
            return false;
        }
        
        return pythonGatewayManager.restart();
    }
}
