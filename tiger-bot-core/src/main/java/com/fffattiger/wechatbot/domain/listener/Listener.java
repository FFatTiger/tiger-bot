package com.fffattiger.wechatbot.domain.listener;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("listeners")
public record Listener(
    @Id
    Long id,

    /**
     * 监听的对象id
     */
    Long chatId,

    /**
     * 是否开启@回复
     */
    boolean atReplyEnable,

    /**
     * 是否开启关键词回复
     */
    boolean keywordReplyEnable,

    /**
     * 是否保存图片
     */
    boolean savePic,

    /**
     * 是否保存语音
     */
    boolean saveVoice,

    /**
     * 是否解析链接
     */
    boolean parseLinks,

    /**
     * 关键词回复
     */
    List<String> keywordReply
) {
}
