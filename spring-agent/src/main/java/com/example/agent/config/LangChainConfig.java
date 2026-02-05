package com.example.agent.config;

import com.example.agent.agent.BacklogAgent;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    @Bean
    public GoogleAiGeminiChatModel googleAiGeminiChatModel(
            @Value("${google-ai.api-key}") String apiKey,
            @Value("${google-ai.model}") String model,
            @Value("${google-ai.timeout-seconds:60}") Integer timeoutSeconds
    ) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Bean
    public BacklogAgent backlogAgent(GoogleAiGeminiChatModel model) {
        System.out.println("=== Creating BacklogAgent with Google AI Gemini ===");
        
        return AiServices.builder(BacklogAgent.class)
                .chatModel(model)
                .build();
    }
}
