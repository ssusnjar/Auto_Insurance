package com.truenorth.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatExecutorService {

    private final JdbcTemplate jdbcTemplate;

    public ChatExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String query) {
        try {

            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

            log.info("Executed query: {}", query);
            log.info("Result count: {}", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing query: {}", query, e);
            throw new RuntimeException("Query execution failed: " + e.getMessage());
        }
    }
}