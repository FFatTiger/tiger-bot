package com.fffattiger.wechatbot.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class CommandInfo {

    /**
     * 是否开启命令
     */
    private boolean commandEnable = true;

    /**
     * 允许的命令
     */
    private List<String> allowCommandPatterns;


    /**
     * 命令权限，key为命令pattern，value为允许的用户
     */
    private Map<String, Set<String>> commandAllowUser;


}
