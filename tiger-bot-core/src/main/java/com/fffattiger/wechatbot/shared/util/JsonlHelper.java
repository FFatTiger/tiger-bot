package com.fffattiger.wechatbot.shared.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonlHelper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 追加jsonl行
     * 
     * @param file
     * @param objs
     * @throws IOException
     */
    public static <T> void appendJsonLines(File file, List<T> objs) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (T obj : objs) {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            sb.append(json).append("\n");
        }
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * 读取jsonl文件所有行，反序列化为对象列表
     * 
     * @param file
     * @param clazz
     * @return
     * @throws IOException
     */
    public static <T> List<T> readJsonLines(File file, Class<T> clazz) throws IOException {
        List<T> result = new ArrayList<>();
        if (!file.exists())
            return result;
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                try {
                    result.add(OBJECT_MAPPER.readValue(line, clazz));
                } catch (Exception e) {
                    
                }
            }
        }
        return result;
    }

    /**
     * 覆盖写入整个jsonl文件
     * 
     * @param file
     * @param objs
     * @throws IOException
     */
    public static void overwriteJsonLines(File file, List<?> objs) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (Object obj : objs) {
                String json = OBJECT_MAPPER.writeValueAsString(obj);
                writer.write(json);
                writer.newLine();
            }
        }
    }

    /**
     * 删除jsonl文件
     * 
     * @param file
     */
    public static void deleteJsonlFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
