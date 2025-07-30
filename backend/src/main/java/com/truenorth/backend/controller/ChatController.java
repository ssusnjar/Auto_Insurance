package com.truenorth.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.truenorth.backend.dto.ChatHistoryDTO;
import com.truenorth.backend.dto.ChatRequestDTO;
import com.truenorth.backend.dto.ChatResponseDTO;
import com.truenorth.backend.service.ChatConversationService;
import com.truenorth.backend.service.ChatHistoryService;
import com.truenorth.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ChatController {

    private final ChatConversationService chatConversationService;
    private final ChatService chatService;
    private final ChatHistoryService chatHistoryService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponseDTO> handleChatMessage(@Valid @RequestBody ChatRequestDTO request) throws JsonProcessingException {

        ChatResponseDTO response = chatService.processMessage(
                request.getConversationId(),
                request.getMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public Page<ChatHistoryDTO> getConversationHistory(@RequestParam(defaultValue = "20") Integer limit, @RequestParam(defaultValue = "0") Integer page) {
        return chatHistoryService.getAllChatHistory(page, limit);
    }

    @GetMapping("history/{id}")
    public ResponseEntity<List<Message>> getConversationHistoryById(@PathVariable String id) {
        return ResponseEntity.ok(chatConversationService.getConversationHistoryById(id));
    }
}