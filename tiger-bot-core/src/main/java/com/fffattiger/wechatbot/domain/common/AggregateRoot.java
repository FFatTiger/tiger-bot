package com.fffattiger.wechatbot.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根的抽象基类。
 * <p>
 * 它继承了 {@link Entity}，从而获得了基于ID的身份标识。
 * <p>
 * 同时，它通过 {@link DomainEvents} 和 {@link AfterDomainEventPublication} 注解，
 * 实现了与 Spring Data 兼容的领域事件发布机制。
 * 这种方式比继承 AbstractAggregateRoot 更灵活，因为它不强制绑定到特定的基类。
 * </p>
 * <p>
 * 使用方法:
 * 1. 在聚合根的方法中，使用 {@code registerEvent(new YourDomainEvent(...));} 来注册事件。
 * 2. 当调用 JpaRepository 的 save 方法时，Spring Data 会自动查找被 {@code @DomainEvents}
 *    注解的方法 (即本类的 events() 方法)，获取并发布所有事件。
 * 3. 发布成功后，Spring Data 会自动调用被 {@code @AfterDomainEventPublication}
 *    注解的方法 (即本类的 clearEvents() 方法)，清空事件列表。
 * </p>
 * 所有聚合根都应继承此类。
 */
@MappedSuperclass
public abstract class AggregateRoot extends Entity {
} 