package com.truenorth.backend.repository;

import com.truenorth.backend.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, String> {

    Page<ChatHistory> findAll(Pageable pageable);
}
