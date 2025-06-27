package com.fffattiger.wechatbot.infrastructure.external.paste;

import java.time.Duration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fffattiger.wechatbot.infrastructure.external.paste.dto.ApiResponse;
import com.fffattiger.wechatbot.infrastructure.external.paste.dto.CreatePasteRequest;
import com.fffattiger.wechatbot.infrastructure.external.paste.dto.HealthResponse;
import com.fffattiger.wechatbot.infrastructure.external.paste.dto.MaxUploadSizeData;
import com.fffattiger.wechatbot.infrastructure.external.paste.dto.PasteData;
import com.fffattiger.wechatbot.infrastructure.external.paste.exception.PasteException;

import lombok.extern.slf4j.Slf4j;

/**
 * CloudPaste服务的客户端实现
 * 使用Spring RestClient与CloudPaste API进行交互
 */
@Component
@Slf4j
public class CloudPasteClient implements PasteClient {
    
    private final RestClient restClient;
    private final String baseBackendUrl;
    private final String baseFrontendUrl;
    private final String apiToken;
    
    public CloudPasteClient(RestClient.Builder restClientBuilder, 
                           CloudPasteProperties properties) {
        this.baseBackendUrl = properties.getBaseBackendUrl();
        this.baseFrontendUrl = properties.getBaseFrontendUrl();
        this.apiToken = properties.getApiToken();
        
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeout()));
        
        this.restClient = restClientBuilder
                .baseUrl(baseBackendUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "ApiKey " + apiToken)
                .build();
    }
    
    @Override
    public HealthResponse health() {
        try {
            log.debug("调用健康检查接口: {}/api/health", baseBackendUrl);
            return restClient.get()
                    .uri("/api/health")
                    .retrieve()
                    .body(HealthResponse.class);
        } catch (RestClientException e) {
            log.error("健康检查失败", e);
            throw new PasteException("健康检查失败", e);
        }
    }
    
    @Override
    public ApiResponse<MaxUploadSizeData> getMaxUploadSize() {
        try {
            log.debug("获取最大上传大小: {}/api/system/max-upload-size", baseBackendUrl);
            return restClient.get()
                    .uri("/api/system/max-upload-size")
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<MaxUploadSizeData>>() {});
        } catch (RestClientException e) {
            log.error("获取最大上传大小失败", e);
            throw new PasteException("获取最大上传大小失败", e);
        }
    }
    
    @Override
    public PasteData createPaste(CreatePasteRequest request) {
        try {
            log.debug("创建文本分享: {}/api/paste", baseBackendUrl);
            RestClient.RequestHeadersSpec<?> requestSpec = restClient.post()
                    .uri("/api/paste")
                    .body(request);
            
            if (apiToken != null && !apiToken.isEmpty()) {
                requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, "ApiKey " + apiToken);
            }
                
            PasteData response = requestSpec.retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (response != null) {
                response.setUrl(baseFrontendUrl + "paste/" + response.getSlug());
            }
            
            return response;
        } catch (RestClientException e) {
            log.error("创建文本分享失败", e);
            throw new PasteException("创建文本分享失败", e);
        }
    }
} 