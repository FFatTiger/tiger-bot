package com.fffattiger.wechatbot.config;

import java.util.List;

import lombok.Data;

@Data
public class WxChatConfig {

    /**
     * 对话的基础信息
     * @return
     */
    private WxInfo wxInfo;

    /**
     * AI聊天信息
     * @return
     */
    private AiChatInfo aiChatInfo;

    /**
     * 命令信息
     * @return
     */
    private CommandInfo commandInfo;

    /**
     * 超级用户列表
     * @return
     */
    private List<String> superUserList;

    /**
     * 是否监听
     */
    private boolean isListen;

    /**
     * 开启@回复
     */
    private boolean atReplyEnable;

    /**
     * 开启关键词回复
     */
    private boolean keywordReplyEnable;

    /**
     * 关键词回复
     */
    private List<String> keywordReply;


    public static WxChatConfig defaultConfig(String chatName) {
        WxChatConfig wxChatConfig = new WxChatConfig();
        WxInfo wxInfo = new WxInfo();
        wxInfo.setChatName(chatName);
        wxChatConfig.setWxInfo(wxInfo);
        return wxChatConfig;
    }
}