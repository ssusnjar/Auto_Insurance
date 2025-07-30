package com.truenorth.backend.service;

import com.truenorth.backend.dto.ChatResponseDTO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatConversationService {
    List<Message> getConversationHistoryById(String conversationId);
}
