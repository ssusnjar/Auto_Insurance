package com.truenorth.backend.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

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
}