package com.truenorth.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truenorth.backend.dto.ChatResponseDTO;
import com.truenorth.backend.model.ChatHistory;
import com.truenorth.backend.model.ChatResponse;
import com.truenorth.backend.repository.ChatHistoryRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;
    private final ChatExecutorService chatExecutorService;
    private final ChatHistoryRepository chatHistoryRepository;

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

    public ChatServiceImpl(ChatModel chatModel, ChatMemory chatMemory, ChatExecutorService chatExecutorService,
                           ObjectMapper objectMapper, ChatHistoryRepository chatHistoryRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatMemory = chatMemory;
        this.objectMapper = objectMapper;
        this.chatExecutorService = chatExecutorService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public ChatResponseDTO processMessage(String conversationId, String userMessage) {
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();

            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setConversationId(conversationId);
            chatHistory.setTitle(userMessage);
            chatHistoryRepository.save(chatHistory);

            log.info("Generated new conversation ID: {}", conversationId);
        }

        chatMemory.add(conversationId, new UserMessage(userMessage));
        List<Message> fullHistory = chatMemory.get(conversationId);

        log.info("Processing message for conversation: {} - Message: {}", conversationId, userMessage);

        try {
            ChatResponse aiResponse = chatClient.prompt()
                    .system(this.systemPromptString)
                    .messages(fullHistory)
                    .advisors(new SimpleLoggerAdvisor())
                    .call()
                    .entity(ChatResponse.class);

            ChatResponseDTO dto = convertToDTO(aiResponse);

            chatMemory.add(conversationId, new AssistantMessage(objectMapper.writeValueAsString(dto)));

            dto.setConversationId(conversationId);
            return dto;

        } catch (Exception e) {
            log.error("Error processing message: ", e);
            return createErrorResponse("An error occurred while processing your request. Please try again.");
        }
    }

    private ChatResponseDTO convertToDTO(ChatResponse aiResponse) {
        ChatResponseDTO dto = new ChatResponseDTO();

        if (aiResponse == null || !aiResponse.isValid()) {
            dto.setValid(false);
            dto.setErrorMessage(aiResponse != null ? aiResponse.getErrorMessage() :
                    "Unable to process your request. Please ask about customer data, demographics, or insurance information.");
            return dto;
        }

        try {
            if (aiResponse.getQuery() != null && !aiResponse.getQuery().isBlank()) {
                List<Map<String, Object>> queryResults = chatExecutorService.executeQuery(aiResponse.getQuery());
                dto.setData(queryResults);

                if (shouldCalculateSummary(aiResponse.getVisualizationType())) {
                    dto.setSummary(calculateDataSummary(queryResults, aiResponse.getChartConfig()));
                }
            }

            dto.setVisualizationType(aiResponse.getVisualizationType());
            dto.setExplanation(aiResponse.getExplanation());
            dto.setValid(true);

            dto.setChartConfig(convertChartConfig(aiResponse.getChartConfig()));

        } catch (Exception e) {
            log.error("Error executing query or mapping response: ", e);
            dto.setValid(false);
            dto.setErrorMessage("Error executing database query: " + e.getMessage());
        }

        return dto;
    }

    private ChatResponseDTO.ChartConfigDTO convertChartConfig(ChatResponse.ChartConfig config) {
        if (config == null) return null;

        ChatResponseDTO.ChartConfigDTO configDTO = new ChatResponseDTO.ChartConfigDTO();

        configDTO.setTitle(config.getTitle());
        configDTO.setSubtitle(config.getSubtitle());

        if (config.getColumns() != null) {
            configDTO.setColumns(List.of(config.getColumns()));
        }
        configDTO.setColumnLabels(config.getColumnLabels());

        configDTO.setXAxisLabel(config.getXAxisLabel());
        configDTO.setYAxisLabel(config.getYAxisLabel());
        configDTO.setXAxisField(config.getXAxisField());
        configDTO.setYAxisField(config.getYAxisField());

        if (config.getSeriesFields() != null) {
            configDTO.setSeriesFields(List.of(config.getSeriesFields()));
        }

        configDTO.setLabelField(config.getLabelField());
        configDTO.setValueField(config.getValueField());

        configDTO.setXField(config.getXField());
        configDTO.setYField(config.getYField());
        configDTO.setSizeField(config.getSizeField());
        configDTO.setCategoryField(config.getCategoryField());

        configDTO.setShowLegend(config.isShowLegend());
        configDTO.setShowDataLabels(config.isShowDataLabels());
        configDTO.setLegendPosition(config.getLegendPosition());
        configDTO.setAdditionalOptions(config.getAdditionalOptions());

        return configDTO;
    }

    private ChatResponseDTO.DataSummary calculateDataSummary(List<Map<String, Object>> data,
                                                             ChatResponse.ChartConfig config) {
        if (data == null || data.isEmpty()) return null;

        ChatResponseDTO.DataSummary summary = new ChatResponseDTO.DataSummary();
        summary.setTotalRecords((long) data.size());

        if (config != null && config.getValueField() != null) {
            String valueField = config.getValueField();

            List<Double> values = data.stream()
                    .map(row -> row.get(valueField))
                    .filter(val -> val instanceof Number)
                    .map(val -> ((Number) val).doubleValue())
                    .collect(Collectors.toList());

            if (!values.isEmpty()) {
                summary.setTotal(values.stream().mapToDouble(Double::doubleValue).sum());
                summary.setAverage(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
                summary.setMin(values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
                summary.setMax(values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
            }
        }

        return summary;
    }

    private boolean shouldCalculateSummary(String visualizationType) {
        return "number".equals(visualizationType) ||
                "table".equals(visualizationType);
    }

    private ChatResponseDTO createErrorResponse(String errorMessage) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setValid(false);
        dto.setErrorMessage(errorMessage);
        return dto;
    }
}