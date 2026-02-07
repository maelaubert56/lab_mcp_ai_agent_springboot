package com.example.agent;

import com.example.agent.mcp.McpHttpClient;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AgentApplicationTests {

	@MockBean
	McpHttpClient mcp;

	@MockBean
	GoogleAiGeminiChatModel geminiModel;

	@Test
	void contextLoads() {
	}

}
