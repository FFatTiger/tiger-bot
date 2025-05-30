package com.fffattiger.wechatbot.config;

import lombok.Data;

@Data
public class WxInfo {

    /**
     * 获取聊天名称
     */
    private String chatName;

    /**
     * 是否为群组
     */
    private boolean groupFlg;
}