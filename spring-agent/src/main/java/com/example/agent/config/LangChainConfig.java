package com.example.agent.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.agent.agent.BacklogAgent;
import com.example.agent.tools.AgentTool;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

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
    public BacklogAgent backlogAgent(GoogleAiGeminiChatModel model, List<AgentTool> tools) {
        System.out.println("=== Agent tools loaded: " + tools.size() + " ===");
        tools.forEach(t -> System.out.println(" - " + t.getClass().getName()));
        
        return AiServices.builder(BacklogAgent.class)
                .chatModel(model)
                .tools(tools.toArray())
                .build();
    }
}
