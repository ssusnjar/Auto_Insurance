package com.truenorth.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truenorth.backend.dto.QueryResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
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

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final QueryExecutorService queryExecutorService;
    private final ObjectMapper objectMapper;

    @Value("${ai.prompt.md.name}")
    private String promptFileName;

    private String systemPromptString;

    @PostConstruct
    public void init() throws IOException {
        Resource systemPromptResource = new ClassPathResource("prompts/" + promptFileName + ".md");
        try (Reader reader = new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            this.systemPromptString = FileCopyUtils.copyToString(reader);
        }
        log.info("Loaded prompt file: {}", promptFileName);
    }

    public ChatService(ChatModel chatModel, ChatMemory chatMemory,
                       QueryExecutorService queryExecutorService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatMemory = chatMemory;
        this.queryExecutorService = queryExecutorService;
        this.objectMapper = new ObjectMapper();
    }

    public QueryResponse processMessage(String conversationId, String userMessage) {
        chatMemory.add(conversationId, new UserMessage(userMessage));

        List<Message> fullHistory = chatMemory.get(conversationId);
        log.info("Processing message for conversation: {}", conversationId);

        String aiResponseContent = Optional.ofNullable(
                chatClient.prompt()
                        .system(this.systemPromptString)
                        .messages(fullHistory)
                        .call()
                        .content()
        ).orElse("");

        chatMemory.add(conversationId, new AssistantMessage(aiResponseContent));

        QueryResponse response = parseQueryResponse(aiResponseContent);

        if (response.isQueryResponse() && response.getQuery() != null) {
            try {
                List<Map<String, Object>> queryResults = queryExecutorService.executeQuery(response.getQuery());
                response.setData(queryResults);

                if (response.getColumns() == null || response.getColumns().isEmpty()) {
                    response.setColumns(queryExecutorService.extractColumns(response.getQuery()));
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

    private QueryResponse parseQueryResponse(String aiResponse) {
        QueryResponse response = new QueryResponse();
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
        } else {
            Pattern sqlPattern = Pattern.compile("```sql\\s*\\n([\\s\\S]*?)\\n\\s*```", Pattern.MULTILINE);
            Matcher sqlMatcher = sqlPattern.matcher(aiResponse);

            if (sqlMatcher.find()) {
                response.setQuery(sqlMatcher.group(1).trim());
                response.setQueryResponse(true);
                response.setVisualizationType("table");
            }
        }

        return response;
    }
}