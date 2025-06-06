package com.fffattiger.wechatbot.shared.properties;

import lombok.Data;

@Data
public class AiChatInfo {

    /**
     * 是否开启AI聊天
     */
    private boolean aiChatEnable;

    /**
     * 供应商+模型
     * 例如：openai:gpt-4o-mini
     */
    private String providerModel;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 模拟的单个打字速度区间
     */
    private double typeOneWordSpeedMin;

    /**
     * 模拟的单个打字速度区间
     */
    private double typeOneWordSpeedMax;

    /**
     * 模拟的打字平均速度
     * @return
     */
    private double typeAvgSpeed;

    /**
     * 每次AI回复的最大行数
     */
    private int maxReplyLines;

    /**
     * 是否开启网络搜索
     */
    private boolean networkSearchEnable;

    /**
     * 是否开启图片分析
     */
    private boolean imageAnalysisEnable;

    /**
     * 收集所有对话内容，如果关闭仅收集@内容
     * @return
     */
    private boolean collectAllMssageEnable;
    
    /**
     * 最大记忆历史
     * @return
     */
    private int maxMemoryHistory;

}
