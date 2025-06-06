package com.fffattiger.wechatbot.management.service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAuto;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 管理服务
 */
@Service
@Slf4j
public class ManagementService {

    @Autowired
    private ChatBotProperties chatBotProperties;
    
    @Autowired(required = false)
    private WxAuto wxAuto;
    
    private Process pythonProcess;

    /**
     * 获取系统状态
     */
    public String getSystemStatus() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            long uptime = runtimeBean.getUptime();
            
            if (uptime > 0) {
                return "运行中 (运行时间: " + formatUptime(uptime) + ")";
            } else {
                return "未知";
            }
        } catch (Exception e) {
            log.error("获取系统状态失败", e);
            return "获取失败";
        }
    }

    /**
     * 获取网关状态
     */
    public String getGatewayStatus() {
        try {
            // 检查Python进程是否存在
            if (pythonProcess != null && pythonProcess.isAlive()) {
                return "运行中";
            }
            
            // 尝试通过网络检查网关是否可用
            if (wxAuto != null) {
                // 这里可以添加ping网关的逻辑
                return "连接中";
            }
            
            return "已停止";
        } catch (Exception e) {
            log.error("获取网关状态失败", e);
            return "未知";
        }
    }

    /**
     * 获取系统指标
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            
            metrics.put("usedMemory", formatBytes(usedMemory));
            metrics.put("maxMemory", formatBytes(maxMemory));
            metrics.put("memoryUsagePercent", (usedMemory * 100) / maxMemory);
            
            // 运行时间
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            metrics.put("uptime", formatUptime(runtimeBean.getUptime()));
            
            // 线程数
            metrics.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
            
        } catch (Exception e) {
            log.error("获取系统指标失败", e);
            metrics.put("error", "获取失败: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 启动Python网关
     */
    public boolean startPythonGateway() {
        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                log.warn("Python网关已在运行中");
                return true;
            }
            
            // 构建Python命令
            ProcessBuilder pb = new ProcessBuilder("python", "wx_http_sse_gateway.py");
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            
            pythonProcess = pb.start();
            
            // 等待一段时间检查进程是否成功启动
            Thread.sleep(2000);
            
            if (pythonProcess.isAlive()) {
                log.info("Python网关启动成功");
                return true;
            } else {
                log.error("Python网关启动失败");
                return false;
            }
            
        } catch (Exception e) {
            log.error("启动Python网关异常", e);
            return false;
        }
    }

    /**
     * 停止Python网关
     */
    public boolean stopPythonGateway() {
        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                pythonProcess.destroy();
                
                // 等待进程结束
                boolean terminated = pythonProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                
                if (!terminated) {
                    // 强制终止
                    pythonProcess.destroyForcibly();
                }
                
                log.info("Python网关已停止");
                return true;
            } else {
                log.warn("Python网关未在运行");
                return true;
            }
            
        } catch (Exception e) {
            log.error("停止Python网关异常", e);
            return false;
        }
    }

    /**
     * 重启Python网关
     */
    public boolean restartPythonGateway() {
        log.info("重启Python网关");
        
        boolean stopped = stopPythonGateway();
        if (!stopped) {
            return false;
        }
        
        // 等待一段时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return startPythonGateway();
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
