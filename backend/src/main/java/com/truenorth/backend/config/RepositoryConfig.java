package com.truenorth.backend.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RepositoryConfig {

    @Value("${db.schema.name}")
    private String dbSchemaName;

    @Bean(name = "jdbcChatMemoryRepository")
    @Primary
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        var dialect = new ChatRepositoryDialect(dbSchemaName);
        return JdbcChatMemoryRepository.builder()
                .dialect(dialect)
                .jdbcTemplate(jdbcTemplate)
                .build();
    }

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public ChatClient fallbackChatClient(
            @Value("${spring.ai.mistralai.api-key}") String apiKey,
            @Value("${spring.ai.mistralai.chat.options.fallback-model}") String modelName,
            @Value("${spring.ai.mistralai.chat.options.temperature}") Double temperature,
            RetryTemplate retryTemplate
    ) {
        MistralAiChatModel mistralAiChatModel = MistralAiChatModel.builder()
                .mistralAiApi(new MistralAiApi(apiKey))
                .defaultOptions(MistralAiChatOptions.builder().model(modelName).temperature(temperature).build())
                .retryTemplate(retryTemplate)
                .observationRegistry(ObservationRegistry.create())
                .build();

        return ChatClient.builder(mistralAiChatModel).build();

    }

}