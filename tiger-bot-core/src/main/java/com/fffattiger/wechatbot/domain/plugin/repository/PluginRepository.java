package com.fffattiger.wechatbot.domain.plugin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.plugin.Plugin;
import com.fffattiger.wechatbot.domain.shared.valueobject.PluginStatus;

public interface PluginRepository extends CrudRepository<Plugin, Long> {
    
    /**
     * 根据插件状态查询
     */
    List<Plugin> findByStatus(PluginStatus status);

    Optional<Plugin> findByPluginId(String pluginId);

}
