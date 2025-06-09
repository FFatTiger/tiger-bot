package com.fffattiger.wechatbot.domain.ai;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.RestClient;

public class ChatClientBuilderFactory {

    public static ChatClient.Builder builder(AiProvider aiProvider, AiModel aiModel, AiRole aiRole,
            Map<String, Object> params, ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        ChatModel chatModel = null;
        if (aiProvider.providerType()
                .equals(org.springframework.ai.observation.conventions.AiProvider.DEEPSEEK.value())) {
            DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                    .restClientBuilder(restClientBuilderProvider.getIfAvailable(RestClient::builder))
                    .apiKey(aiProvider.apiKey()).build();

            chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi)
                    .defaultOptions(DeepSeekChatOptions.builder()
                            .model(aiModel.modelName())
                            // .temperature(aiProvider.temperature())
                            // .maxTokens(aiProvider.maxTokens())
                            // .frequencyPenalty(aiProvider.frequencyPenalty())
                            // .topP(aiProvider.topP())
                            .build())
                    .build();
        } else if (aiProvider.providerType()
                .equals(org.springframework.ai.observation.conventions.AiProvider.OPENAI.value())) {
            OpenAiApi openAiApi = OpenAiApi.builder().restClientBuilder(restClientBuilderProvider.getIfAvailable(RestClient::builder)).baseUrl(aiProvider.baseUrl()).apiKey(aiProvider.apiKey()).build();
            chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(aiModel.modelName())
                            .build())
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid provider type: " + aiProvider.providerType());
        }

        ChatClient.Builder builder = ChatClient.builder(chatModel);
        switch (MessageType.fromValue(aiRole.promptType())) {
            case SYSTEM:
                builder.defaultSystem(t -> t.text(aiRole.promptContent()).params(params));
                break;
            case USER:
                builder.defaultUser(t -> t.text(aiRole.promptContent()).params(params));
                break;
            default:
                throw new IllegalArgumentException("Invalid prompt type: " + aiRole.promptType());
        }

        return builder;
    }
}
