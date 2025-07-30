package com.truenorth.backend.service;

import com.truenorth.backend.dto.ChatHistoryDTO;
import com.truenorth.backend.model.ChatHistory;
import org.springframework.data.domain.Page;

public interface ChatHistoryService {

    Page<ChatHistoryDTO> getAllChatHistory(Integer page, Integer limit);
    ChatHistory saveChatHistory(String conversationId, String title);
}
