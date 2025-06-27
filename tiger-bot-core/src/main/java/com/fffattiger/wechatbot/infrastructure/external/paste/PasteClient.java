package com.fffattiger.wechatbot.infrastructure.external.paste;

import com.fffattiger.wechatbot.infrastructure.external.paste.dto.*;

/**
 * Paste服务客户端接口
 * 提供文本分享服务的基本操作，支持不同实现的扩展
 */
public interface PasteClient {
    
    /**
     * 健康检查
     * @return 健康状态信息
     */
    HealthResponse health();
    
    /**
     * 获取系统最大上传文件大小
     * @return 最大上传大小信息
     */
    ApiResponse<MaxUploadSizeData> getMaxUploadSize();
    
    /**
     * 创建新的文本分享
     * @param request 创建请求
     * @return 创建的文本分享信息
     */
    PasteData createPaste(CreatePasteRequest request);
    
    /**
     * 创建简单的文本分享（仅包含内容）
     * @param content 文本内容
     * @return 创建的文本分享信息
     */
    default PasteData createSimplePaste(String content) {
        CreatePasteRequest request = new CreatePasteRequest(content);
        return createPaste(request);
    }
} 