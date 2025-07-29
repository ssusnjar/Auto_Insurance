package com.truenorth.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truenorth.backend.dto.ChatResponseDTO;
import com.truenorth.backend.model.ChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
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

@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;
    private final ChatExecutorService chatExecutorService;

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

    public ChatService(ChatModel chatModel, ChatMemory chatMemory, ChatExecutorService chatExecutorService, ObjectMapper objectMapper) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatMemory = chatMemory;
        this.objectMapper = objectMapper;
        this.chatExecutorService = chatExecutorService;
    }

    public ChatResponseDTO  processMessage(String conversationId, String userMessage) throws JsonProcessingException {
        ChatResponseDTO chatResponseDTO = new ChatResponseDTO();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = java.util.UUID.randomUUID().toString();
        }

        chatMemory.add(conversationId, new UserMessage(userMessage));

        List<Message> fullHistory = chatMemory.get(conversationId);
        log.info("Processing message for conversation: {}", conversationId);

        ChatResponse aiResponseContent = chatClient.prompt()
                .system(this.systemPromptString)
                .messages(fullHistory)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .entity(ChatResponse.class);


        chatMemory.add(conversationId, new AssistantMessage(objectMapper.writeValueAsString(aiResponseContent)));

        if (aiResponseContent != null && aiResponseContent.getQuery() != null) {
            chatResponseDTO.setVisualizationType(aiResponseContent.getVisualizationType());
            chatResponseDTO.setExplanation(aiResponseContent.getExplanation());
            chatResponseDTO.setColumns(chatExecutorService.extractColumns(aiResponseContent.getQuery()));
            chatResponseDTO.setData(chatExecutorService.executeQuery(aiResponseContent.getQuery()));
            chatResponseDTO.setTitle(aiResponseContent.getTitle());
            chatResponseDTO.setXAxis(aiResponseContent.getXAxis());
            chatResponseDTO.setYAxis(aiResponseContent.getYAxis());
            chatResponseDTO.setExplanation(aiResponseContent.getExplanation());
            chatResponseDTO.setQueryResponse(true);
        } else {
            log.warn("AI response or query was null.");
            chatResponseDTO.setError(true);
        }
        return chatResponseDTO;
    }
}