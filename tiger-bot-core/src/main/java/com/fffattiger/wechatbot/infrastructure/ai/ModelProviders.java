package com.fffattiger.wechatbot.infrastructure.ai;

import java.util.Arrays;

public enum ModelProviders {

	ANTHROPIC("anthropic"),

	AZURE_OPENAI("azure-openai"),

	BEDROCK_COHERE("bedrock-cohere"),

	BEDROCK_CONVERSE("bedrock-converse"),

	BEDROCK_TITAN("bedrock-titan"),

	HUGGINGFACE("huggingface"),

	MINIMAX("minimax"),

	MISTRAL("mistral"),

	OCI_GENAI("oci-genai"),

	OLLAMA("ollama"),

	OPENAI("openai"),

	POSTGRESML("postgresml"),

	STABILITY_AI("stabilityai"),

	TRANSFORMERS("transformers"),

	VERTEX_AI("vertexai"),

	ZHIPUAI("zhipuai"),

	DEEPSEEK("deepseek");

	private final String provider;

	ModelProviders(String provider) {
		this.provider = provider;
	}

	public String getProvider() {
		return provider;
	}

	public static ModelProviders getByProvider(String provider) {
		return Arrays.stream(ModelProviders.values())
				.filter(modelProvider -> modelProvider.getProvider().equalsIgnoreCase(provider))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid model provider: " + provider));
	}
}
