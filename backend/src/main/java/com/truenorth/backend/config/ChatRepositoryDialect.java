package com.truenorth.backend.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;

public class ChatRepositoryDialect implements JdbcChatMemoryRepositoryDialect {

    private final String schema;

    public ChatRepositoryDialect(String schema) {
        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("Schema name cannot be null or empty.");
        }
        this.schema = schema;
    }

    @Override
    public String getSelectMessagesSql() {
        return "SELECT content, type FROM " + this.schema + ".SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\"";
    }

    @Override
    public String getInsertMessageSql() {
        return "INSERT INTO " + this.schema + ".SPRING_AI_CHAT_MEMORY (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, ?)";
    }

    @Override
    public String getSelectConversationIdsSql() {
        return "SELECT DISTINCT conversation_id FROM " + this.schema + ".SPRING_AI_CHAT_MEMORY";
    }

    @Override
    public String getDeleteMessagesSql() {
        return "DELETE FROM " + this.schema + ".SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?";
    }
}
