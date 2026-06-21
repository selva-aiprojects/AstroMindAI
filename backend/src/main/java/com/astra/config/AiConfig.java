package com.astra.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@Slf4j
public class AiConfig {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${anthropic.api.key:}")
    private String anthropicKey;

    @Value("${google.ai.api.key:}")
    private String googleAiKey;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        if (groqApiKey != null && !groqApiKey.isEmpty() && !groqApiKey.equals("${GROQ_API_KEY}")) {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(groqApiKey)
                    .modelName(groqModel)
                    .temperature(0.7)
                    .maxTokens(2048)
                    .timeout(Duration.ofSeconds(30))
                    .build();
            log.info("Initialized Groq Chat Model ({})", groqModel);
            return model;
        }

        if (openAiKey != null && !openAiKey.isEmpty() && !openAiKey.equals("${OPENAI_API_KEY}")) {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(openAiKey)
                    .modelName("gpt-4o")
                    .temperature(0.7)
                    .maxTokens(1024)
                    .timeout(Duration.ofSeconds(30))
                    .build();
            log.info("Initialized OpenAI Chat Model (GPT-4o)");
            return model;
        }

        log.warn("No valid LLM API keys configured. Using simulated AI responses.");
        return OpenAiChatModel.builder()
                .apiKey("sk-placeholder")
                .modelName("gpt-4o")
                .temperature(0.7)
                .maxTokens(1024)
                .timeout(Duration.ofSeconds(30))
                .build();
    }

}
