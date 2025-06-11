package com.fffattiger.wechatbot.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.fffattiger.wechatbot")
@Slf4j
public class TigerBotManagementApplication {

    public static void main(String[] args) {
        log.info("=== Tiger Bot Management 管理后台启动中 ===");
        log.info("Java版本: {}", System.getProperty("java.version"));
        log.info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));

        try {
            SpringApplication.run(TigerBotManagementApplication.class, args);
            log.info("=== Tiger Bot Management 管理后台启动成功 ===");
        } catch (Exception e) {
            log.error("=== Tiger Bot Management 管理后台启动失败 ===", e);
            System.exit(1);
        }
    }
}
