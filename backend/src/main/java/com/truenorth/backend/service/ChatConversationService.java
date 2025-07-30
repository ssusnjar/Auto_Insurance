package com.truenorth.backend.service;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatConversationService {
    List<Message> getConversationHistoryById(String conversationId);
}
