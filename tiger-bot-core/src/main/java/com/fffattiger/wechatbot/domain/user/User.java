package com.fffattiger.wechatbot.domain.user;

import com.fffattiger.wechatbot.domain.common.AggregateRoot;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AggregateRoot {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户备注
     */
    private String remark;

    // 构造函数
    protected User() {}

    public User(String username, String remark) {
        this.username = username;
        this.remark = remark;
    }

    // 业务方法
    public boolean isValidUser() {
        return username != null && !username.trim().isEmpty();
    }

    public boolean hasRole(String role) {
        // 简单的角色检查，实际可能需要更复杂的实现
        return "ADMIN".equals(role) && username != null && username.contains("admin");
    }

    public String getDisplayName() {
        if (remark != null && !remark.trim().isEmpty()) {
            return remark;
        }
        return username;
    }
}