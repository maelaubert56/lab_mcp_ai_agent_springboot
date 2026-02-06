package com.example.agent.tools;

import com.example.agent.mcp.McpHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GitHubMcpToolsTest {

    @Test
    void should_call_mcp_client_with_correct_parameters() {
        // Given
        McpHttpClient mcp = mock(McpHttpClient.class);
        Map<String, Object> mockResponse = Map.of(
                "number", 42,
                "html_url", "https://github.com/maelaubert56/lab_mcp_ai_agent_springboot/issues/42"
        );
        when(mcp.callTool(eq("issue_write"), anyMap()))
                .thenReturn(Mono.just(mockResponse));

        GitHubMcpTools tools = new GitHubMcpTools(mcp, "maelaubert56", "lab_mcp_ai_agent_springboot");

        // When
        String result = tools.createIssue("Test Issue", "This is a test body");

        // Then
        assertThat(result).contains("Issue created successfully");
        assertThat(result).contains("42");

        // Verify the MCP client was called with correct tool name
        verify(mcp, times(1)).callTool(eq("issue_write"), anyMap());
    }

    @Test
    void should_pass_correct_arguments_to_mcp_client() {
        // Given
        McpHttpClient mcp = mock(McpHttpClient.class);
        when(mcp.callTool(anyString(), anyMap()))
                .thenReturn(Mono.just(Map.of("number", 1)));

        GitHubMcpTools tools = new GitHubMcpTools(mcp, "test-owner", "test-repo");

        // When
        tools.createIssue("My Title", "My Body");

        // Then
        verify(mcp).callTool(
                eq("issue_write"),
                argThat(args -> {
                    Map<String, Object> map = (Map<String, Object>) args;
                    return "create".equals(map.get("method"))
                            && "test-owner".equals(map.get("owner"))
                            && "test-repo".equals(map.get("repo"))
                            && "My Title".equals(map.get("title"))
                            && "My Body".equals(map.get("body"));
                })
        );
    }

    @Test
    void should_handle_successful_response() {
        // Given
        McpHttpClient mcp = mock(McpHttpClient.class);
        Map<String, Object> response = Map.of(
                "number", 123,
                "html_url", "https://github.com/owner/repo/issues/123",
                "title", "Test Issue"
        );
        when(mcp.callTool(anyString(), anyMap()))
                .thenReturn(Mono.just(response));

        GitHubMcpTools tools = new GitHubMcpTools(mcp, "owner", "repo");

        // When
        String result = tools.createIssue("Title", "Body");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("Issue created successfully");
        assertThat(result).contains("123");
    }
}
