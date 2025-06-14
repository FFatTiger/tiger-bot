package com.fffattiger.wechatbot.management.controller;

import com.fffattiger.wechatbot.application.service.PluginApplicationService;
import com.fffattiger.wechatbot.management.application.dto.PluginManagementDto;
import com.fffattiger.wechatbot.management.application.service.PluginManagementApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 插件管理控制器
 * 
 * @author liguoxian
 */
@Slf4j
@Controller
@RequestMapping("/management/plugin-config")
@RequiredArgsConstructor
public class PluginManagementController {

    private final PluginManagementApplicationService pluginManagementService;
    private final PluginApplicationService pluginApplicationService;

    /**
     * 插件管理主页面
     */
    @GetMapping("/plugins")
    public String pluginsPage(Model model) {
        try {
            // 获取所有插件信息
            List<PluginManagementDto> plugins = pluginManagementService.getAllPlugins();
            
            // 获取统计信息
            var statistics = pluginApplicationService.getStatistics();
            
            // 计算各状态插件数量
            long enabledCount = plugins.stream()
                    .filter(p -> "ENABLED".equals(p.status().name()))
                    .count();
            long disabledCount = plugins.stream()
                    .filter(p -> "DISABLED".equals(p.status().name()))
                    .count();
            long errorCount = plugins.stream()
                    .filter(p -> "ERROR".equals(p.status().name()))
                    .count();

            // 传递数据到模板
            model.addAttribute("plugins", plugins);
            model.addAttribute("totalCount", plugins.size());
            model.addAttribute("enabledCount", enabledCount);
            model.addAttribute("disabledCount", disabledCount);
            model.addAttribute("errorCount", errorCount);
            model.addAttribute("statistics", statistics);

            return "management/plugin-config/plugins";
        } catch (Exception e) {
            log.error("获取插件列表失败", e);
            model.addAttribute("error", "获取插件列表失败: " + e.getMessage());
            return "management/plugin-config/plugins";
        }
    }

    /**
     * 获取单个插件详情 (API)
     */
    @GetMapping("/plugins/{pluginId}")
    @ResponseBody
    public ResponseEntity<PluginManagementDto> getPlugin(@PathVariable String pluginId) {
        try {
            PluginManagementDto plugin = pluginManagementService.getPluginById(pluginId);
            return ResponseEntity.ok(plugin);
        } catch (Exception e) {
            log.error("获取插件详情失败: {}", pluginId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 上传插件文件
     */
    @PostMapping(value = "/plugins/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String uploadPlugin(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "请选择要上传的文件";
            }

            String result = pluginManagementService.uploadPlugin(file);
            if ("success".equals(result)) {
                log.info("插件上传成功: {}", file.getOriginalFilename());
                return "success";
            } else {
                log.warn("插件上传失败: {}, 原因: {}", file.getOriginalFilename(), result);
                return result;
            }
        } catch (Exception e) {
            log.error("插件上传异常: {}", file.getOriginalFilename(), e);
            return "上传失败: " + e.getMessage();
        }
    }

    /**
     * 启用插件
     */
    @PostMapping(value = "/plugins/{pluginId}/enable", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String enablePlugin(@PathVariable String pluginId) {
        try {
            pluginManagementService.enablePlugin(pluginId);
            log.info("插件启用成功: {}", pluginId);
            return "success";
        } catch (Exception e) {
            log.error("插件启用失败: {}", pluginId, e);
            return "启用失败: " + e.getMessage();
        }
    }

    /**
     * 禁用插件
     */
    @PostMapping(value = "/plugins/{pluginId}/disable", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String disablePlugin(@PathVariable String pluginId) {
        try {
            pluginManagementService.disablePlugin(pluginId);
            log.info("插件禁用成功: {}", pluginId);
            return "success";
        } catch (Exception e) {
            log.error("插件禁用失败: {}", pluginId, e);
            return "禁用失败: " + e.getMessage();
        }
    }

    /**
     * 更新插件参数
     */
    @PostMapping(value = "/plugins/{pluginId}/parameters", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String updatePluginParameters(@PathVariable String pluginId, 
                                       @RequestBody Map<String, Object> request) {
        try {
            String parameters = (String) request.get("parameters");
            if (parameters == null) {
                return "参数不能为空";
            }

            pluginManagementService.updatePluginParameters(pluginId, parameters);
            log.info("插件参数更新成功: {}", pluginId);
            return "success";
        } catch (Exception e) {
            log.error("插件参数更新失败: {}", pluginId, e);
            return "参数更新失败: " + e.getMessage();
        }
    }

    /**
     * 删除插件
     */
    @DeleteMapping(value = "/plugins/{pluginId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String deletePlugin(@PathVariable String pluginId) {
        try {
            pluginManagementService.deletePlugin(pluginId);
            log.info("插件删除成功: {}", pluginId);
            return "success";
        } catch (Exception e) {
            log.error("插件删除失败: {}", pluginId, e);
            return "删除失败: " + e.getMessage();
        }
    }

    /**
     * 获取插件统计信息 (API)
     */
    @GetMapping("/plugins/statistics")
    @ResponseBody
    public ResponseEntity<Object> getPluginStatistics() {
        try {
            var statistics = pluginApplicationService.getStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取插件统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}