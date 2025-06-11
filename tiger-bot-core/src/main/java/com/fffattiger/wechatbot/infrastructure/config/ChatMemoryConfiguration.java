package com.fffattiger.wechatbot.infrastructure.config;
// package com.fffattiger.wechatbot.config;

// import com.fffattiger.wechatbot.ai.JsonChatMemoryRepository;
// import com.fffattiger.wechatbot.properties.ChatBotProperties;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.chat.memory.ChatMemory;
// import org.springframework.ai.chat.memory.ChatMemoryRepository;
// import org.springframework.ai.chat.memory.MessageWindowChatMemory;
// import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
// import org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryAutoConfiguration;
// import org.springframework.beans.factory.ObjectProvider;
// import org.springframework.boot.autoconfigure.AutoConfigureAfter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.util.function.Supplier;

// @Configuration
// @AutoConfigureAfter(JdbcChatMemoryRepositoryAutoConfiguration.class)
// @Slf4j
// public class ChatMemoryConfiguration {

//     @Bean
//     public ChatMemory chatMemory(
//             ChatBotProperties chatBotProperties,
//             ObjectProvider<JdbcChatMemoryRepository> jdbcChatMemoryRepositoryProvider
//     ) {
//         ChatMemoryRepository repository = jdbcChatMemoryRepositoryProvider.getIfAvailable();
//         if (repository != null) {
//             
//             return MessageWindowChatMemory.builder()
//                     .chatMemoryRepository(repository)
//                     .maxMessages(chatBotProperties.getChatMemoryFileMaxCount())
//                     .build();
//         } else {
//             
//             return MessageWindowChatMemory.builder()
//                     .chatMemoryRepository(new JsonChatMemoryRepository(chatBotProperties.getChatMemoryDir()))
//                     .maxMessages(chatBotProperties.getChatMemoryFileMaxCount())
//                     .build();
//         }
//     }
// }
