package com.fffattiger.wechatbot.infrastructure.external.paste.exception;

/**
 * Paste服务异常
 */
public class PasteException extends RuntimeException {
    
    private final Integer code;
    private final String message;
    
    public PasteException(String message) {
        super(message);
        this.code = null;
        this.message = message;
    }
    
    public PasteException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public PasteException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
        this.message = message;
    }
    
    public PasteException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        if (code != null) {
            return String.format("[%d] %s", code, message);
        }
        return message;
    }
} 