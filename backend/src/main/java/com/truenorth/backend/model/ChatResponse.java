package com.truenorth.backend.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChatResponse {
    private String message;
    private QueryResult queryResult;

    @Data
    @Builder
    public static class QueryResult {
        private List<Map<String, Object>> data;
        private ChartType chartType;
        private List<String> columns;
        private String sqlQuery;
    }

    public enum ChartType {
        BAR, LINE, PIE, TABLE, NONE
    }
}
