package com.fffattiger.wechatbot.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public class User {
    @Id
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户备注
     */
    private String remark;

    // 构造函数
    public User() {}

    public User(Long id, String username, String remark) {
        this.id = id;
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

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRemark() {
        return remark;
    }

    // Setters (仅用于框架)
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}