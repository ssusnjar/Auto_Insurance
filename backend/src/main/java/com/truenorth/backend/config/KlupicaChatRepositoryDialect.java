package com.truenorth.backend.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;


public class KlupicaChatRepositoryDialect implements JdbcChatMemoryRepositoryDialect {

    @Override
    public String getSelectMessagesSql() {
        return "SELECT content, type FROM klupica.SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\"";
    }
    @Override
    public String getInsertMessageSql() {
        return "INSERT INTO klupica.SPRING_AI_CHAT_MEMORY (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, ?)";
    }

    @Override
    public String getSelectConversationIdsSql() {
        return "SELECT DISTINCT conversation_id FROM klupica.SPRING_AI_CHAT_MEMORY";
    }

    @Override
    public String getDeleteMessagesSql() {
        return "DELETE FROM klupica.SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?";
    }
}
