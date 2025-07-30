package com.truenorth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history", schema = "klupica")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatHistory {

    @Id
    @Column(name = "conversation_id", length = 255, nullable = false)
    String conversationId;

    @Column(name = "title", length = 500)
    String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    public ChatHistory(String conversationId, String title) {
        this.conversationId = conversationId;
        this.title = title;
    }
}