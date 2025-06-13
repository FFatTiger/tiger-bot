package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.repository.AiProviderRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI提供商应用服务
 * 负责AI提供商的业务逻辑处理
 */
@Service
@Slf4j
public class AiProviderApplicationService {

    @Resource
    private AiProviderRepository aiProviderRepository;

    /**
     * 获取所有AI提供商
     */
    public List<AiProvider> getAllProviders() {
        List<AiProvider> providers = new ArrayList<>();
        aiProviderRepository.findAll().forEach(providers::add);
        log.debug("获取所有AI提供商，共{}个", providers.size());
        return providers;
    }

    /**
     * 根据ID获取AI提供商
     */
    public Optional<AiProvider> getProviderById(Long id) {
        return aiProviderRepository.findById(id);
    }

    /**
     * 创建AI提供商
     */
    public AiProvider createProvider(String providerType, String providerName, String apiKey, String baseUrl) {
        if (providerType == null || providerType.trim().isEmpty()) {
            throw new IllegalArgumentException("提供商类型不能为空");
        }
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("提供商名称不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API密钥不能为空");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("基础URL不能为空");
        }

        AiProvider newProvider = new AiProvider(null, providerType.trim(), providerName.trim(), 
                                               apiKey.trim(), baseUrl.trim());
        AiProvider savedProvider = aiProviderRepository.save(newProvider);
        
        return savedProvider;
    }

    /**
     * 更新AI提供商
     */
    public AiProvider updateProvider(Long id, String providerType, String providerName, String apiKey, String baseUrl) {
        aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI提供商不存在: " + id));

        if (providerType == null || providerType.trim().isEmpty()) {
            throw new IllegalArgumentException("提供商类型不能为空");
        }
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("提供商名称不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API密钥不能为空");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("基础URL不能为空");
        }

        AiProvider updatedProvider = new AiProvider(id, providerType.trim(), providerName.trim(), 
                                                   apiKey.trim(), baseUrl.trim());
        AiProvider savedProvider = aiProviderRepository.save(updatedProvider);
        
        return savedProvider;
    }

    /**
     * 删除AI提供商
     */
    public void deleteProvider(Long id) {
        if (!aiProviderRepository.existsById(id)) {
            throw new RuntimeException("AI提供商不存在: " + id);
        }
        
        aiProviderRepository.deleteById(id);
        
    }

    /**
     * 检查提供商是否存在
     */
    public boolean existsById(Long id) {
        return aiProviderRepository.existsById(id);
    }
}
