package com.fffattiger.wechatbot.management.infrastructure.gateway;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;

import lombok.extern.slf4j.Slf4j;

/**
 * Python网关管理器
 * 负责Python网关进程的启停控制
 */
@Component
@Slf4j
public class PythonGatewayManager {
    
    @Autowired(required = false)
    private WxAuto wxAuto;
    
    private Process pythonProcess;

    /**
     * 获取网关状态
     */
    public String getStatus() {
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
            
            metrics.put("error", "获取失败: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 启动Python网关
     */
    public boolean start() {
        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                
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
                
                return true;
            } else {
                
                return false;
            }
            
        } catch (Exception e) {
            
            return false;
        }
    }

    /**
     * 停止Python网关
     */
    public boolean stop() {
        try {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                pythonProcess.destroy();
                
                // 等待进程结束
                boolean terminated = pythonProcess.waitFor(5, TimeUnit.SECONDS);
                
                if (!terminated) {
                    // 强制终止
                    pythonProcess.destroyForcibly();
                }
                
                
                return true;
            } else {
                
                return true;
            }
            
        } catch (Exception e) {
            
            return false;
        }
    }

    /**
     * 重启Python网关
     */
    public boolean restart() {
        
        
        boolean stopped = stop();
        if (!stopped) {
            return false;
        }
        
        // 等待一段时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return start();
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟", minutes);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
