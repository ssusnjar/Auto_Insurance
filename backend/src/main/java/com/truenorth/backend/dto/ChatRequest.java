package com.truenorth.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message cannot be null or empty.")
    private String message;

    @NotBlank(message = "Conversation ID cannot be null or empty.")
    private String conversationId;
}