package com.fffattiger.wechatbot.infrastructure.startup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fffattiger.wechatbot.shared.properties.DatabaseInitProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库启动初始化器
 * 在应用启动时检查数据库表是否存在，如果不存在则创建表并初始化数据
 */
@RequiredArgsConstructor
@Slf4j
@Order(1) // 确保在其他组件之前执行
@Component
public class DatabaseStartupInitializer implements OrderedInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final DatabaseInitProperties properties;
    
    /**
     * 系统需要的所有表名
     */
    private static final List<String> REQUIRED_TABLES = Arrays.asList(
        "ai_providers", "ai_models", "ai_roles", "chats", 
        "commands", "users", "chat_command_auths", "listeners", "messages"
    );
    

    @Override
    public void init() {
        if (!properties.isEnabled()) {
            log.info("数据库初始化已禁用，跳过检查");
            return;
        }

        log.info("=== 数据库启动初始化检查开始 ===");

        try {
            if (properties.isForceRecreate()) {
                log.warn("强制重新创建模式已启用，将删除所有表并重新创建");
                dropAllTables();
                createTablesAndInitData();
            } else if (!checkAllTablesExist()) {
                log.info("检测到表缺失，开始创建表并初始化数据");
                createTablesAndInitData();
            } else {
                log.info("所有必需的表都存在，无需初始化");
            }

            log.info("=== 数据库启动初始化检查完成 ===");

        } catch (Exception e) {
            log.error("=== 数据库初始化失败 ===", e);
            throw new RuntimeException("数据库初始化失败，应用无法启动", e);
        }
    }
    
    /**
     * 检查所有必需的表是否存在
     */
    private boolean checkAllTablesExist() {
        log.info("检查数据库表结构...");
        
        for (String tableName : REQUIRED_TABLES) {
            if (!tableExists(tableName)) {
                log.warn("表 {} 不存在", tableName);
                return false;
            }
        }
        
        log.info("所有必需的表都存在");
        return true;
    }
    
    /**
     * 检查指定表是否存在
     */
    private boolean tableExists(String tableName) {
        try {
            String sql = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = ?
                )
                """;
            
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            log.error("检查表 {} 是否存在时发生错误", tableName, e);
            return false;
        }
    }
    
    /**
     * 删除所有表（仅在强制重建模式下使用）
     */
    private void dropAllTables() {
        log.warn("开始删除所有表...");
        
        // 倒序删除表以避免外键约束问题
        for (int i = REQUIRED_TABLES.size() - 1; i >= 0; i--) {
            String tableName = REQUIRED_TABLES.get(i);
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName + " CASCADE");
                log.info("删除表: {}", tableName);
            } catch (Exception e) {
                log.error("删除表 {} 时发生错误", tableName, e);
            }
        }
    }
    
    /**
     * 创建表并初始化数据
     */
    private void createTablesAndInitData() {
        // 创建表结构
        executeScript(properties.getSchemaLocation(), "创建表结构");

        // 检查是否需要初始化数据
        if (isTableEmpty("ai_providers")) {
            executeScript(properties.getDataLocation(), "初始化数据");
        } else {
            log.info("检测到数据库已有数据，跳过数据初始化");
        }
    }
    
    /**
     * 执行SQL脚本 - 使用Spring的ResourceDatabasePopulator方式
     */
    private void executeScript(String location, String description) {
        log.info("开始{}...", description);
        
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IOException("SQL脚本文件不存在: " + location);
            }
            
            log.info("找到SQL脚本文件: {}", location);
            
            // 使用Spring Boot的ResourceDatabasePopulator
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            populator.setIgnoreFailedDrops(true);
            populator.setContinueOnError(false);  // 遇到错误时停止执行
            
            // 执行脚本
            populator.execute(jdbcTemplate.getDataSource());
            
            log.info("{}完成", description);
            
        } catch (Exception e) {
            log.error("{}失败: {}", description, e.getMessage(), e);
            throw new RuntimeException(description + "失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查表是否为空
     */
    private boolean isTableEmpty(String tableName) {
        try {
            if (!tableExists(tableName)) {
                return true;
            }
            
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            boolean isEmpty = count == null || count == 0;
            
            log.debug("表 {} 记录数: {}", tableName, count);
            return isEmpty;
            
        } catch (Exception e) {
            log.error("检查表 {} 是否为空时发生错误", tableName, e);
            return true;
        }
    }

    @Override
    public int getOrder() {
        return -999;
    }
}
