package com.fffattiger.wechatbot.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fffattiger.wechatbot")
public class TigerBotManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TigerBotManagementApplication.class, args);
    }

}
