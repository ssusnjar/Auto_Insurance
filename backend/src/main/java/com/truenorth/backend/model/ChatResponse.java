package com.truenorth.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    @JsonProperty("sqlQuery")
    @JsonPropertyDescription("Generated SQL query to fetch data")
    private String query;

    @JsonProperty("visualizationType")
    @JsonPropertyDescription("Type of visualization: table, bar, line, pie, scatter, number, doughnut, radar, polarArea")
    private String visualizationType;

    @JsonProperty("chartConfig")
    @JsonPropertyDescription("Dynamic chart configuration based on visualization type")
    private ChartConfig chartConfig;

    @JsonProperty("explanation")
    @JsonPropertyDescription("Explanation of what the data represents")
    private String explanation;

    @JsonProperty("isValid")
    @JsonPropertyDescription("Whether the prompt is valid and related to database")
    private boolean isValid;

    @JsonProperty("errorMessage")
    @JsonPropertyDescription("Error message if prompt is invalid")
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartConfig {
        private String title;
        private String subtitle;

        private String[] columns;
        private Map<String, String> columnLabels;

        private String xAxisLabel;
        private String yAxisLabel;
        private String xAxisField;
        private String yAxisField;
        private String[] seriesFields;

        private String labelField;
        private String valueField;

        private String xField;
        private String yField;
        private String sizeField;
        private String categoryField;

        private boolean showLegend;
        private boolean showDataLabels;
        private String legendPosition;
        private Map<String, Object> additionalOptions;
    }
}