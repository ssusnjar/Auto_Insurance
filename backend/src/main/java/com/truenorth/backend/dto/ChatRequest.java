package com.truenorth.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message cannot be null or empty.")
    @Schema(description = "User message in natural language, e.g. 'Show average income by marital status'")
    private String message;

    @NotBlank(message = "Conversation ID cannot be null or empty.")
    @Schema(description = "Conversation context identifier (should be unique for eg. uuid)")
    private String conversationId;
}