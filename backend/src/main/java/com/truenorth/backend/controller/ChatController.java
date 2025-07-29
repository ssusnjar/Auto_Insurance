package com.truenorth.backend.controller;

import com.truenorth.backend.dto.ChatRequest;
import com.truenorth.backend.dto.ChatResponse;
import com.truenorth.backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> handleChatMessage(@Valid @RequestBody ChatRequest request) {

        ChatResponse response = chatService.processMessage(
                request.getConversationId(),
                request.getMessage());

        return ResponseEntity.ok(response);
    }
}