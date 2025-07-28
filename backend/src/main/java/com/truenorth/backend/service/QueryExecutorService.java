package com.truenorth.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
public class QueryExecutorService {

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String query) {
        try {
            String trimmedQuery = query.trim().toUpperCase();


            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

            log.info("Executed query: {}", query);
            log.info("Result count: {}", results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing query: {}", query, e);
            throw new RuntimeException("Query execution failed: " + e.getMessage());
        }
    }

    public List<String> extractColumns(String query) {
        List<String> columns = new ArrayList<>();

        try {
            String upperQuery = query.toUpperCase();
            int selectIndex = upperQuery.indexOf("SELECT");
            int fromIndex = upperQuery.indexOf("FROM");

            if (selectIndex != -1 && fromIndex != -1) {
                String selectClause = query.substring(selectIndex + 6, fromIndex).trim();
                String[] parts = selectClause.split(",");

                for (String part : parts) {
                    part = part.trim();
                    if (part.toUpperCase().contains(" AS ")) {
                        String[] aliasParts = part.split("(?i)\\s+as\\s+");
                        if (aliasParts.length > 1) {
                            columns.add(aliasParts[1].trim());
                        } else {
                            columns.add(part);
                        }
                    } else {
                        columns.add(part);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting columns from query", e);
        }

        return columns;
    }
}
