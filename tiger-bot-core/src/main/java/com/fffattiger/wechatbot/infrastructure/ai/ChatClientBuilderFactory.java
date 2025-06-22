package com.fffattiger.wechatbot.infrastructure.ai;

import java.net.URL;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;

public class ChatClientBuilderFactory {

    public static ChatClient.Builder builder(AiProvider aiProvider, AiModel aiModel, AiRole aiRole,
            Map<String, Object> params, ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        ChatModel chatModel = null;
        if (aiProvider.getProviderType()
                .equals(org.springframework.ai.observation.conventions.AiProvider.DEEPSEEK.value())) {
            chatModel = buildDeepSeekModel(aiProvider, aiModel, restClientBuilderProvider);
        } else if (aiProvider.getProviderType()
                .equals(org.springframework.ai.observation.conventions.AiProvider.OPENAI.value())) {
            chatModel = builderOpenAiModel(aiProvider, aiModel, restClientBuilderProvider);
        } else {
            throw new IllegalArgumentException("Invalid provider type: " + aiProvider.getProviderType());
        }

        ChatClient.Builder builder = ChatClient.builder(chatModel);

        if (aiRole != null && StringUtils.hasLength(aiRole.getPromptType()) && StringUtils.hasLength(aiRole.getPromptContent())) {
            switch (MessageType.fromValue(aiRole.getPromptType())) {
                case SYSTEM:
                    builder.defaultSystem(t -> t.text(aiRole.getPromptContent()).params(params));
                    break;
                case USER:
                    builder.defaultUser(t -> t.text(aiRole.getPromptContent()).params(params));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid prompt type: " + aiRole.getPromptType());
            }
        }

        return builder;
    }

    private static ChatModel builderOpenAiModel(AiProvider aiProvider, AiModel aiModel,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        ChatModel chatModel;
        // 处理baseUrl和completionPath
        String baseUrl = aiProvider.getBaseUrl();
        String completionPath = extractCompletionPath(baseUrl);
        OpenAiApi.Builder openAiApiBuilder = OpenAiApi.builder()
                .restClientBuilder(restClientBuilderProvider.getIfAvailable(RestClient::builder))
                .baseUrl(aiProvider.getBaseUrl())
                .apiKey(aiProvider.getApiKey());
        if (completionPath != null) {
            openAiApiBuilder.baseUrl(baseUrl.replace(completionPath, ""));
            openAiApiBuilder.completionsPath(completionPath);
        }
        OpenAiApi openAiApi = openAiApiBuilder.build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(aiModel.getModelName());

        // 应用动态参数配置
        optionsBuilder = ReflectionParameterConfigurer.configureParameters(optionsBuilder, aiModel.getParams());

        chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();
        return chatModel;
    }

    private static ChatModel buildDeepSeekModel(AiProvider aiProvider, AiModel aiModel,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        ChatModel chatModel;
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .restClientBuilder(restClientBuilderProvider.getIfAvailable(RestClient::builder))
                .apiKey(aiProvider.getApiKey()).build();

        // 使用通用反射工具动态配置参数
        DeepSeekChatOptions.Builder optionsBuilder = DeepSeekChatOptions.builder()
                .model(aiModel.getModelName());

        // 应用动态参数配置
        optionsBuilder = ReflectionParameterConfigurer.configureParameters(optionsBuilder, aiModel.getParams());

        chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi)
                .defaultOptions(optionsBuilder.build())
                .build();
        return chatModel;
    }

    private static String extractCompletionPath(String baseUrl) {
        String completionPath = null;
        try {
            URL url = new URL(baseUrl);
            String path = url.getPath();
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                // 如果有路径,则分离baseUrl和completionPath
                baseUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
                completionPath = path;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid baseUrl: " + baseUrl, e);
        }
        return completionPath;
    }
}
