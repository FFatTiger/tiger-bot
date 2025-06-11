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
        
        
        if (!gatewayDomainService.canStartGateway()) {
            
            return false;
        }
        
        return pythonGatewayManager.start();
    }

    /**
     * 停止网关
     */
    public boolean stopGateway() {
        
        
        if (!gatewayDomainService.canStopGateway()) {
            
            return false;
        }
        
        return pythonGatewayManager.stop();
    }

    /**
     * 重启网关
     */
    public boolean restartGateway() {
        
        
        if (!gatewayDomainService.canRestartGateway()) {
            
            return false;
        }
        
        return pythonGatewayManager.restart();
    }
}
