package com.example.agent.web;

import com.example.agent.mcp.McpHttpClient;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AgentControllerIT {

    @Autowired
    WebTestClient web;

    @MockBean
    McpHttpClient mcp;

    @MockBean
    GoogleAiGeminiChatModel geminiModel;

    @Test
    void should_process_agent_request_through_full_stack() {
        // Given - Mock Gemini response (simulating tool call decision)
        dev.langchain4j.data.message.AiMessage aiResponse = 
            dev.langchain4j.data.message.AiMessage.from("Issue created successfully: {number=42, html_url=https://github.com/maelaubert56/lab_mcp_ai_agent_springboot/issues/42}");
        
        when(geminiModel.chat(org.mockito.ArgumentMatchers.any(dev.langchain4j.model.chat.request.ChatRequest.class)))
                .thenReturn(dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .aiMessage(aiResponse)
                        .build());

        // Mock MCP response
        Map<String, Object> mockMcpResponse = Map.of(
                "number", 42,
                "html_url", "https://github.com/maelaubert56/lab_mcp_ai_agent_springboot/issues/42",
                "title", "Test Issue"
        );
        when(mcp.callTool(eq("issue_write"), anyMap()))
                .thenReturn(Mono.just(mockMcpResponse));

        // When - Call the agent endpoint
        Map<String, String> request = Map.of(
                "prompt", "Create a task to add OpenTelemetry and export traces via OTLP"
        );

        // Then - Verify the response
        web.post()
                .uri("/api/agent/run")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .consumeWith(response -> {
                    Map<String, Object> body = response.getResponseBody();
                    assert body != null;
                    assert body.containsKey("response");
                    String responseText = (String) body.get("response");
                    assert responseText != null;
                    // The agent should mention the issue was created
                    assert responseText.length() > 0;
                });
    }

    @Test
    void should_handle_simple_prompt() {
        // Given - Mock Gemini response
        dev.langchain4j.data.message.AiMessage aiResponse = 
            dev.langchain4j.data.message.AiMessage.from("Issue created: https://github.com/test/repo/issues/1");
        
        when(geminiModel.chat(org.mockito.ArgumentMatchers.any(dev.langchain4j.model.chat.request.ChatRequest.class)))
                .thenReturn(dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .aiMessage(aiResponse)
                        .build());

        // Mock MCP response
        when(mcp.callTool(eq("issue_write"), anyMap()))
                .thenReturn(Mono.just(Map.of(
                        "number", 1,
                        "html_url", "https://github.com/test/repo/issues/1"
                )));

        // When
        Map<String, String> request = Map.of(
                "prompt", "Create an issue for adding Docker support"
        );

        // Then
        web.post()
                .uri("/api/agent/run")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void should_return_error_when_prompt_is_missing() {
        // When - Send empty request
        web.post()
                .uri("/api/agent/run")
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
