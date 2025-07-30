package com.truenorth.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.truenorth.backend.dto.ChatResponseDTO;

public interface ChatService {
    ChatResponseDTO processMessage(String conversationId, String userMessage) throws JsonProcessingException;
}
