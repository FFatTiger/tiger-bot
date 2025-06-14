package com.fffattiger.wechatbot.management.application.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.fffattiger.wechatbot.application.service.PluginApplicationService;
import com.fffattiger.wechatbot.domain.plugin.Plugin;
import com.fffattiger.wechatbot.domain.plugin.PluginLoaderService;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginLoadType;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginType;
import com.fffattiger.wechatbot.management.application.dto.PluginManagementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 插件管理应用服务
 * 协调层 - 连接管理界面和核心服务
 * 
 * @author liguoxian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PluginManagementApplicationService {

    private final PluginApplicationService pluginApplicationService;

    /**
     * 获取所有插件（转换为DTO）
     */
    public List<PluginManagementDto> getAllPlugins() {
        List<Plugin> plugins = pluginApplicationService.getAllPlugins();
        return plugins.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取插件
     */
    public PluginManagementDto getPluginById(String pluginId) {
        Plugin plugin = pluginApplicationService.getPluginById(pluginId);
        return convertToDto(plugin);
    }

    /**
     * 上传插件文件
     */
    public String uploadPlugin(MultipartFile file) {
        try {
            // 1. 验证文件
            validatePluginFile(file);
            
            // 2. 保存文件到本地
            Path savedFilePath = savePluginFile(file);
            
            // 3. 创建插件实体
            Plugin plugin = createPluginFromFile(file, savedFilePath);
            
            // 4. 保存到数据库
            pluginApplicationService.save(plugin);
            
            log.info("插件上传成功: {}", plugin.getName());
            return "success";
            
        } catch (Exception e) {
            log.error("插件上传失败: {}", file.getOriginalFilename(), e);
            return e.getMessage();
        }
    }

    /**
     * 启用插件
     */
    public void enablePlugin(String pluginId) {
        pluginApplicationService.enablePlugin(pluginId);
    }

    /**
     * 禁用插件
     */
    public void disablePlugin(String pluginId) {
        pluginApplicationService.disablePlugin(pluginId);
    }

    /**
     * 更新插件参数
     */
    public void updatePluginParameters(String pluginId, String parameters) {
        pluginApplicationService.updatePluginParameters(pluginId, parameters);
    }

    /**
     * 删除插件
     */
    public void deletePlugin(String pluginId) {
        pluginApplicationService.deletePlugin(pluginId);
    }

    /**
     * 验证插件文件
     */
    private void validatePluginFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".jar")) {
            throw new IllegalArgumentException("文件必须是JAR格式");
        }
        
        // 检查文件大小限制（例如50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过50MB");
        }
    }

    /**
     * 保存插件文件到本地
     */
    private Path savePluginFile(MultipartFile file) throws IOException {
        // 创建插件存储目录
        Path pluginDir = Paths.get("plugins");
        Files.createDirectories(pluginDir);
        
        // 生成唯一文件名，避免冲突
        String originalFileName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + originalFileName;
        Path filePath = pluginDir.resolve(fileName);
        
        // 保存文件
        File pluginFile = filePath.toFile();
        FileUtil.writeFromStream(file.getInputStream(), pluginFile);
        
        return filePath;
    }

    /**
     * 从上传文件创建插件实体
     */
    private Plugin createPluginFromFile(MultipartFile file, Path savedPath) {

        PluginWrapper pluginWrapper = pluginApplicationService.loadPlugin(savedPath);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        Plugin plugin = new Plugin();
        
        // 基本信息
        plugin.setPluginId(pluginDescriptor.getPluginId());
        plugin.setName(pluginDescriptor.getPluginId());
        plugin.setVersion(pluginDescriptor.getVersion());
        plugin.setAuthor(pluginDescriptor.getProvider());
        plugin.setDescription(pluginDescriptor.getPluginDescription());
        
        // 状态和类型
        plugin.setStatus(PluginStatus.DISABLED); // 默认禁用状态
        plugin.setLoadType(PluginLoadType.LOCAL);
        plugin.setPluginType(PluginType.MESSAGE_HANDLER); // 默认类型
        
        // 文件信息
        plugin.setSourcePath(savedPath.toString());
        plugin.setSize((double) file.getSize() / 1024 / 1024); // 转换为MB
        
        // 时间戳
        LocalDateTime now = LocalDateTime.now();
        plugin.setInstalledAt(now);
        plugin.setUpdatedAt(now);
        
        return plugin;
    }

    /**
     * 从文件名提取插件名称
     */
    private String extractPluginName(String fileName) {
        if (fileName == null) {
            return "未知插件";
        }
        
        // 移除扩展名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }
        
        // 移除版本号（简单处理）
        fileName = fileName.replaceAll("-\\d+\\.\\d+.*", "");
        
        return fileName;
    }

    /**
     * 转换Plugin为DTO
     */
    private PluginManagementDto convertToDto(Plugin plugin) {
        return new PluginManagementDto(
            plugin.getPluginId(),
            plugin.getName(),
            plugin.getVersion(),
            plugin.getAuthor(),
            plugin.getDescription(),
            plugin.getStatus(),
            plugin.getLoadType(),
            plugin.getParameters(),
            plugin.getSize(),
            plugin.getInstalledAt(),
            plugin.getUpdatedAt(),
            plugin.getPluginType() == PluginType.COMMAND_HANDLER
        );
    }
}