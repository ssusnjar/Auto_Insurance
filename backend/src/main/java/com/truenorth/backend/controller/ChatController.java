package com.truenorth.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.truenorth.backend.dto.ChatRequestDTO;
import com.truenorth.backend.dto.ChatResponseDTO;
import com.truenorth.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponseDTO> handleChatMessage(@Valid @RequestBody ChatRequestDTO request) throws JsonProcessingException {

        ChatResponseDTO response = chatService.processMessage(
                request.getConversationId(),
                request.getMessage()
        );

        return ResponseEntity.ok(response);
    }
}