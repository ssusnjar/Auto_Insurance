package com.truenorth.backend.controller;

import com.truenorth.backend.dto.ChatRequest;
import com.truenorth.backend.dto.QueryResponse;
import com.truenorth.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @Operation(
            summary = "Send a user message and receive a database query response",
            description = """
                Accepts a natural language message (e.g. "Prikaži mi prosječni iznos plaćen za korisnike koji imaju djecu") 
                and returns the AI-generated SQL query, suggested chart type, explanation, axis labels, and result data.
                The backend interprets user intent, generates SQL, executes it, and returns both the query and visualization info.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully generated a query and returned results",
            content = @Content(schema = @Schema(implementation = QueryResponse.class))
    )
    @PostMapping
    public ResponseEntity<QueryResponse> handleChatMessage(@Valid @RequestBody ChatRequest request) {

        QueryResponse response = chatService.processMessage(
                request.getConversationId(),
                request.getMessage()
        );

        return ResponseEntity.ok(response);
    }
}