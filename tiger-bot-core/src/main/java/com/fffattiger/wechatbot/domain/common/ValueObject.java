package com.fffattiger.wechatbot.domain.common;

import jakarta.persistence.MappedSuperclass;

/**
 * 值对象（Value Object）的抽象基类。
 * <p>
 * 值对象的核心特征是它们没有唯一标识（ID），并通过其所有属性值的组合来定义相等性。
 * 它们应该是不可变的（Immutable）。
 * </p>
 * <p>
 * 这个基类主要作为一个标记，表明其子类是一个值对象。
 * 实现值对象时，强烈建议使用Java的 {@code record} 类型，因为它自动提供了正确的
 * equals(), hashCode() 和 toString() 方法，并保证了不可变性。
 * </p>
 * <pre>
 * {@code
 * public record Address(String street, String city) extends ValueObject {
 *     // record 自动处理了所有事情
 * }
 * }
 * </pre>
 */
@MappedSuperclass
public abstract class ValueObject {

    // 值对象的 equals 和 hashCode 应该比较所有字段。
    // 让子类去实现这个，或者最好直接使用 record。
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
} 