package com.truenorth.backend.repository;

import com.truenorth.backend.model.QueryResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueryResultRepository extends JpaRepository<QueryResultEntity, Long> {
    Optional<QueryResultEntity> findTopByConversationIdOrderByCreatedAtDesc(String conversationId);

    List<QueryResultEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);
}
