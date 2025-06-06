package com.fffattiger.wechatbot.domain.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

@Table("commands")
public record Command(
    @Id
    Long id,

    /**
     * 命令的正则或名称
     */
    String pattern,

    /**
     * 命令描述
     */
    String description,

    /**
     * AI配置
     */
    @Embedded.Empty
    AiSpecification aiSpecification
) {} 