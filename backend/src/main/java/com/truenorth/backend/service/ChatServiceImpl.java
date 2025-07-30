package com.truenorth.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truenorth.backend.dto.ChatResponseDTO;
import com.truenorth.backend.exception.SqlQueryFailedException;
import com.truenorth.backend.model.ChatHistory;
import com.truenorth.backend.model.ChatResponse;
import com.truenorth.backend.repository.ChatHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
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

    private static final int MAX_RETRY_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final ChatClient fallbackChatClient;
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

    public ChatServiceImpl(ChatMemory chatMemory, ChatExecutorService chatExecutorService,
                           ObjectMapper objectMapper, ChatHistoryRepository chatHistoryRepository,
                           ChatClient chatClient, @Qualifier("fallbackChatClient") ChatClient fallbackChatClient) {
        this.chatMemory = chatMemory;
        this.objectMapper = objectMapper;
        this.chatExecutorService = chatExecutorService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatClient = chatClient;
        this.fallbackChatClient = fallbackChatClient;
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

        try {
            log.info("Processing message with primary client for conversation: {}", conversationId);
            ChatResponse aiResponse = chatClient.prompt()
                    .system(this.systemPromptString)
                    .messages(fullHistory)
                    .advisors(new SimpleLoggerAdvisor())
                    .call()
                    .entity(ChatResponse.class);

            ChatResponseDTO dto = convertToDTO(aiResponse);
            chatMemory.add(conversationId, new AssistantMessage(objectMapper.writeValueAsString(aiResponse)));
            dto.setConversationId(conversationId);
            return dto;

        } catch (SqlQueryFailedException e) {
            log.warn("Primary AI generated invalid SQL. Attempting fallback with retry logic. Query: '{}'. Error: '{}'",
                    e.getFailedQuery(), e.getMessage());

            return attemptFallbackWithRetry(conversationId, fullHistory, e.getFailedQuery(), e.getMessage(), 1);

        } catch (Exception e) {
            log.error("An unexpected error occurred while processing message: ", e);
            return createErrorResponse("An unexpected error occurred. Please try again.");
        }
    }

    private ChatResponseDTO attemptFallbackWithRetry(String conversationId, List<Message> fullHistory,
                                                     String failedQuery, String errorMessage, int attemptNumber) {

        if (attemptNumber > MAX_RETRY_ATTEMPTS) {
            log.error("Maximum retry attempts ({}) exceeded. Unable to generate valid SQL query.", MAX_RETRY_ATTEMPTS);
            return createErrorResponse("We tried multiple times to process your request but were unable to generate a valid query. Please try rephrasing your question or check if the requested data exists.");
        }

        log.info("Fallback attempt {} for conversation: {}", attemptNumber, conversationId);

        String recoveryInstruction = buildRecoveryInstruction(failedQuery, errorMessage, attemptNumber);

        List<Message> retryHistory = List.copyOf(fullHistory);
        retryHistory.add(new UserMessage(recoveryInstruction));

        try {
            ChatResponse fallbackAiResponse = fallbackChatClient.prompt()
                    .system(this.systemPromptString)
                    .messages(retryHistory)
                    .advisors(new SimpleLoggerAdvisor())
                    .call()
                    .entity(ChatResponse.class);

            ChatResponseDTO dto = convertToDTO(fallbackAiResponse);
            chatMemory.add(conversationId, new AssistantMessage(objectMapper.writeValueAsString(fallbackAiResponse)));
            dto.setConversationId(conversationId);
            return dto;

        } catch (SqlQueryFailedException retryException) {
            log.warn("Fallback attempt {} failed. Query: '{}'. Error: '{}'. Retrying...",
                    attemptNumber, retryException.getFailedQuery(), retryException.getMessage());

            return attemptFallbackWithRetry(conversationId, fullHistory,
                    retryException.getFailedQuery(), retryException.getMessage(), attemptNumber + 1);

        } catch (Exception fallbackException) {
            log.error("Fallback attempt {} failed with unexpected error.", attemptNumber, fallbackException);

            if (attemptNumber < MAX_RETRY_ATTEMPTS) {
                return attemptFallbackWithRetry(conversationId, fullHistory, failedQuery,
                        "Unexpected error: " + fallbackException.getMessage(), attemptNumber + 1);
            } else {
                return createErrorResponse("We tried multiple times to process your request but encountered technical difficulties. Please try again later.");
            }
        }
    }

    private String buildRecoveryInstruction(String failedQuery, String errorMessage, int attemptNumber) {
        StringBuilder instruction = new StringBuilder();

        if (attemptNumber == 1) {
            instruction.append("The previous SQL query attempt failed. You need to analyze the error carefully and generate a corrected query.\n\n");
        } else {
            instruction.append(String.format("This is retry attempt #%d. The previous query still failed. Please analyze more carefully.\n\n", attemptNumber));
        }

        instruction.append("**FAILED SQL QUERY:**\n");
        instruction.append("```sql\n").append(failedQuery).append("\n```\n\n");

        instruction.append("**DATABASE ERROR:**\n");
        instruction.append("'").append(errorMessage).append("'\n\n");

        instruction.append("**INSTRUCTIONS FOR CORRECTION:**\n");
        instruction.append("1. **REFLECT ON DATABASE SCHEMA**: Carefully review the available tables, columns, and their exact names. Common issues include:\n");
        instruction.append("   - Incorrect table names (check spelling and case sensitivity)\n");
        instruction.append("   - Wrong column names or missing columns\n");
        instruction.append("   - Incorrect data types in WHERE clauses or JOINs\n");
        instruction.append("   - Missing table aliases or incorrect alias usage\n\n");

        instruction.append("2. **ANALYZE THE ERROR**: Based on the database error message:\n");
        instruction.append("   - If it mentions 'table doesn't exist' or 'unknown column', verify schema names\n");
        instruction.append("   - If it's a syntax error, check SQL grammar and formatting\n");
        instruction.append("   - If it's a data type mismatch, ensure proper casting or comparison\n");
        instruction.append("   - If it's a constraint violation, check for valid relationships\n\n");

        instruction.append("3. **SELF-CORRECTION PROCESS**:\n");
        instruction.append("   - Compare your failed query against the known database schema\n");
        instruction.append("   - Identify the specific issue causing the error\n");
        instruction.append("   - Generate a new, corrected query that addresses the root cause\n");
        instruction.append("   - Ensure your new query is syntactically correct and uses valid table/column names\n\n");

        instruction.append("4. **VALIDATION CHECKLIST**:\n");
        instruction.append("   - All table names exist and are spelled correctly\n");
        instruction.append("   - All column names are valid for their respective tables\n");
        instruction.append("   - JOIN conditions are properly structured\n");
        instruction.append("   - WHERE clause conditions use appropriate data types\n");
        instruction.append("   - Aggregate functions are used correctly with GROUP BY\n\n");

        instruction.append("Please provide a complete, corrected response that addresses the user's original request with a valid SQL query.");

        return instruction.toString();
    }

    private ChatResponseDTO convertToDTO(ChatResponse aiResponse) throws SqlQueryFailedException, JsonProcessingException {
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
            String errorMessage = e.getMessage();
            log.warn("SQL query execution failed. Query: [{}], Error: [{}]", aiResponse.getQuery(), errorMessage);
            throw new SqlQueryFailedException(errorMessage, e, aiResponse.getQuery());
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