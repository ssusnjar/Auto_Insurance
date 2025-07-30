package com.truenorth.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatConversationServiceImpl implements ChatConversationService {

    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;


    public List<Message> getConversationHistoryById(String conversationId) {
        return chatMemory.get(conversationId);
    }
}
