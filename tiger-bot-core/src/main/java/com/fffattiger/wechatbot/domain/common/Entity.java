package com.fffattiger.wechatbot.domain.common;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * 实体（Entity）的抽象基类。
 * <p>
 * 提供了所有实体共有的ID属性，并重写了 equals 和 hashCode 方法，
 * 以确保实体的相等性是基于其唯一标识（ID）来判断的，而不是基于属性值。
 * </p>
 * 所有实体，无论是聚合根还是聚合内部的实体，都应继承此类。
 */
@MappedSuperclass
public abstract class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        if (this.id == null || entity.id == null) {
            return false;
        }
        return id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
} 