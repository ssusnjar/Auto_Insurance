package com.truenorth.backend.service;

import com.truenorth.backend.dto.ChatHistoryDTO;
import com.truenorth.backend.model.ChatHistory;
import com.truenorth.backend.repository.ChatHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Override
    public Page<ChatHistoryDTO> getAllChatHistory(Integer page, Integer limit) {
        return chatHistoryRepository.findAll(PageRequest.of(page, limit))
                .map(chatHistory -> new ChatHistoryDTO(chatHistory.getConversationId(), chatHistory.getTitle()));
    }

    @Override
    public ChatHistory saveChatHistory(String conversationId, String title) {
        ChatHistory chatHistory = new ChatHistory(conversationId, title, LocalDateTime.now());
        return chatHistoryRepository.save(chatHistory);
    }
}