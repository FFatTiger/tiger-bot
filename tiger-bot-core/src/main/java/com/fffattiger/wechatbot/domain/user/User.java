package com.fffattiger.wechatbot.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(
    @Id
    Long id,

    /**
     * 用户名
     */
    String username,

    /**
     * 用户备注
     */
    String remark
) {} 