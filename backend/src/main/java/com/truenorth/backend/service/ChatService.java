package com.truenorth.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truenorth.backend.dto.ChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ChatService {

    private final ChatModel chatModel;
    private  ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatExecutorService chatExecutorService;
    private final ObjectMapper objectMapper;

    @Value("${ai.prompt.md.name}")
    private String promptFileName;

    @PostConstruct
    public void init() throws IOException {
        Resource systemPromptResource = new ClassPathResource("prompts/" + promptFileName + ".md");
        String systemPromptString;
        try (Reader reader = new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            systemPromptString = FileCopyUtils.copyToString(reader);
        }
        log.info("Loaded prompt file: {}", promptFileName);

        this.chatClient = ChatClient.builder(this.chatModel)
                .defaultSystem(systemPromptString)
                .build();
    }


    public ChatService(ChatModel chatModel, ChatMemory chatMemory,
                       ChatExecutorService chatExecutorService) {
        this.chatMemory = chatMemory;
        this.chatExecutorService = chatExecutorService;
        this.objectMapper = new ObjectMapper();
        this.chatModel = chatModel;
    }

    public ChatResponse processMessage(String conversationId, String userMessage) {

        log.info("Processing message for conversation: {}", conversationId);

        String aiResponseContent = Optional.ofNullable(
                chatClient.prompt()
                        .user(userMessage)
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(conversationId).build())
                        .call()
                        .content()
        ).orElse("");

        ChatResponse response = parseQueryResponse(aiResponseContent);

        if (response.isQueryResponse() && response.getQuery() != null) {
            try {
                List<Map<String, Object>> queryResults = chatExecutorService.executeQuery(response.getQuery());
                response.setData(queryResults);

                if (response.getColumns() == null || response.getColumns().isEmpty()) {
                    response.setColumns(chatExecutorService.extractColumns(response.getQuery()));
                }

                log.info("Query executed successfully with {} results", queryResults.size());
            } catch (Exception e) {
                log.error("Error executing query", e);
                response.setError("Failed to execute query: " + e.getMessage());
                response.setData(null);
            }
        }

        return response;
    }

    private ChatResponse parseQueryResponse(String aiResponse) {
        ChatResponse response = new ChatResponse();
        response.setQueryResponse(false);

        Pattern jsonPattern = Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n\\s*```", Pattern.MULTILINE);
        Matcher jsonMatcher = jsonPattern.matcher(aiResponse);

        if (jsonMatcher.find()) {
            try {
                String jsonContent = jsonMatcher.group(1);
                Map<String, Object> queryData = objectMapper.readValue(jsonContent, Map.class);

                response.setQuery((String) queryData.get("query"));
                response.setVisualizationType((String) queryData.get("visualizationType"));
                response.setExplanation((String) queryData.get("explanation"));
                response.setColumns((List<String>) queryData.get("columns"));
                response.setTitle((String) queryData.get("title"));
                response.setXAxis((String) queryData.get("xAxis"));
                response.setYAxis((String) queryData.get("yAxis"));
                response.setQueryResponse(true);

            } catch (Exception e) {
                log.error("Error parsing query response JSON", e);
            }
        }

        return response;
    }
}