package com.fffattiger.wechatbot.infrastructure.ai;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用的反射参数配置工具类
 * 支持解析类似 "temperature:0.9;topP:0.1;" 格式的参数字符串，并通过反射动态调用对象的方法
 */
@Slf4j
public class ReflectionParameterConfigurer {
    

    /**
     * 通用的参数配置方法
     * 
     * @param <T> builder对象的类型
     * @param builder 要配置的builder对象
     * @param paramsString 参数字符串，格式如："temperature:0.9;topP:0.1;"
     * @return 配置后的builder对象
     */
    public static <T> T configureParameters(T builder, String paramsString) {
        if (builder == null) {
            log.warn("Builder对象为null，跳过参数配置");
            return builder;
        }
        
        if (paramsString == null || paramsString.trim().isEmpty()) {
            log.info("参数字符串为空，跳过参数配置");
            return builder;
        }
        
        try {
            Map<String, String> params = parseParams(paramsString);
            Class<?> builderClass = builder.getClass();
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String methodName = entry.getKey();
                String value = entry.getValue();
                
                if (methodName == null || methodName.trim().isEmpty()) {
                    log.warn("方法名为空，跳过: " + entry);
                    continue;
                }
                
                if (value == null) {
                    log.warn("参数值为null，跳过方法: " + methodName);
                    continue;
                }
                
                try {
                    invokeBuilderMethod(builder, builderClass, methodName.trim(), value);
                } catch (Exception e) {
                    log.warn("设置参数失败 " + methodName + " = " + value + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("解析参数字符串时发生异常: " + e.getMessage());
        }
        
        return builder;
    }
    
    /**
     * 解析参数字符串为Map
     * 
     * @param paramsString 参数字符串，格式如："temperature:0.9;topP:0.1;"
     * @return 参数Map
     * @throws IllegalArgumentException 当参数格式不正确时
     */
    private static Map<String, String> parseParams(String paramsString) throws IllegalArgumentException {
        Map<String, String> params = new HashMap<>();
        
        if (paramsString == null || paramsString.trim().isEmpty()) {
            return params;
        }
        
        try {
            // 按分号分割参数
            String[] pairs = paramsString.split(";");
            
            for (String pair : pairs) {
                if (pair.trim().isEmpty()) {
                    continue;
                }
                
                // 按冒号分割键值对
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) {
                    log.warn("忽略格式不正确的参数对: " + pair + " (应该是 key:value 格式)");
                    continue;
                }
                
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                if (key.isEmpty()) {
                    log.warn("忽略空的参数名: " + pair);
                    continue;
                }
                
                params.put(key, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("解析参数字符串失败: " + paramsString, e);
        }
        
        return params;
    }
    
    /**
     * 通过反射调用builder方法
     * 
     * @param builder builder实例
     * @param builderClass builder类
     * @param methodName 方法名
     * @param value 参数值
     * @throws Exception 反射异常
     */
    private static void invokeBuilderMethod(Object builder, Class<?> builderClass, 
            String methodName, String value) throws Exception {
        
        // 尝试Double类型参数
        if (tryInvokeMethod(builder, builderClass, methodName, value, Double.class, ReflectionParameterConfigurer::parseDouble)) {
            return;
        }
        
        // 尝试Integer类型参数 (严格整数转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, Integer.class, ReflectionParameterConfigurer::parseInteger)) {
            return;
        }
        
        // 尝试int类型参数 (严格整数转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, int.class, ReflectionParameterConfigurer::parseInteger)) {
            return;
        }
        
        // 尝试Long类型参数 (严格长整数转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, Long.class, ReflectionParameterConfigurer::parseLong)) {
            return;
        }
        
        // 尝试long类型参数 (严格长整数转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, long.class, ReflectionParameterConfigurer::parseLong)) {
            return;
        }
        
        // 尝试Float类型参数
        if (tryInvokeMethod(builder, builderClass, methodName, value, Float.class, ReflectionParameterConfigurer::parseFloat)) {
            return;
        }
        
        // 尝试float类型参数
        if (tryInvokeMethod(builder, builderClass, methodName, value, float.class, ReflectionParameterConfigurer::parseFloat)) {
            return;
        }
        
        // 尝试Boolean类型参数 (严格布尔转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, Boolean.class, ReflectionParameterConfigurer::parseBoolean)) {
            return;
        }
        
        // 尝试boolean类型参数 (严格布尔转换)
        if (tryInvokeMethod(builder, builderClass, methodName, value, boolean.class, ReflectionParameterConfigurer::parseBoolean)) {
            return;
        }
        
        // 尝试String类型参数
        if (tryInvokeMethod(builder, builderClass, methodName, value, String.class, s -> s)) {
            return;
        }
        
        throw new NoSuchMethodException("找不到适合的方法 " + methodName + " 来处理值 " + value);
    }
    
    /**
     * 尝试调用指定类型的方法
     * 
     * @param builder builder实例
     * @param builderClass builder类
     * @param methodName 方法名
     * @param value 参数值字符串
     * @param paramType 参数类型
     * @param parser 值解析器
     * @return 是否成功调用
     */
    private static <P> boolean tryInvokeMethod(Object builder, Class<?> builderClass, 
            String methodName, String value, Class<P> paramType, ValueParser<P> parser) {
        try {
            Method method = builderClass.getMethod(methodName, paramType);
            P parsedValue = parser.parse(value);
            method.invoke(builder, parsedValue);
            log.info("成功设置参数 " + methodName + " = " + parsedValue + " (" + paramType.getSimpleName() + ")");
            return true;
        } catch (NoSuchMethodException e) {
            // 方法不存在，继续尝试其他类型
            return false;
        } catch (Exception e) {
            // 参数解析失败或方法调用失败
            log.warn("调用方法 " + methodName + " 失败 (" + paramType.getSimpleName() + "): " + e.getMessage());
            return false;
        }
    }
    
    // 自定义解析方法，加强类型检查
    
    /**
     * 严格的Double解析
     */
    private static Double parseDouble(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("空值无法转换为Double");
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法将 '" + value + "' 转换为Double: " + e.getMessage());
        }
    }
    
    /**
     * 严格的Integer解析 - 不允许小数
     */
    private static Integer parseInteger(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("空值无法转换为Integer");
        }
        
        String trimmed = value.trim();
        
        // 检查是否包含小数点
        if (trimmed.contains(".")) {
            throw new NumberFormatException("不能将包含小数点的值 '" + value + "' 转换为Integer");
        }
        
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法将 '" + value + "' 转换为Integer: " + e.getMessage());
        }
    }
    
    /**
     * 严格的Long解析 - 不允许小数
     */
    private static Long parseLong(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("空值无法转换为Long");
        }
        
        String trimmed = value.trim();
        
        // 检查是否包含小数点
        if (trimmed.contains(".")) {
            throw new NumberFormatException("不能将包含小数点的值 '" + value + "' 转换为Long");
        }
        
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法将 '" + value + "' 转换为Long: " + e.getMessage());
        }
    }
    
    /**
     * 严格的Float解析
     */
    private static Float parseFloat(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("空值无法转换为Float");
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法将 '" + value + "' 转换为Float: " + e.getMessage());
        }
    }
    
    /**
     * 严格的Boolean解析 - 只接受 true/false (忽略大小写)
     */
    private static Boolean parseBoolean(String value) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("空值无法转换为Boolean");
        }
        
        String trimmed = value.trim().toLowerCase();
        
        if ("true".equals(trimmed)) {
            return true;
        } else if ("false".equals(trimmed)) {
            return false;
        } else {
            throw new IllegalArgumentException("无法将 '" + value + "' 转换为Boolean，只接受 'true' 或 'false' (忽略大小写)");
        }
    }
    
    /**
     * 值解析器函数式接口
     */
    @FunctionalInterface
    private interface ValueParser<T> {
        T parse(String value) throws Exception;
    }
} 