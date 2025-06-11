package com.fffattiger.wechatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TigerBotCoreApplication {

    public static void main(String[] args) {
        log.info("=== Tiger Bot Core 应用启动中 ===");
        log.info("Java版本: {}", System.getProperty("java.version"));
        log.info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));

        try {
            SpringApplication.run(TigerBotCoreApplication.class, args);
            log.info("=== Tiger Bot Core 应用启动成功 ===");
        } catch (Exception e) {
            log.error("=== Tiger Bot Core 应用启动失败 ===", e);
            System.exit(1);
        }
    }
}