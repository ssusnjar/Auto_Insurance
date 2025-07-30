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
        @JsonProperty("title")
        private String title;

        @JsonProperty("subtitle")
        private String subtitle;

        @JsonProperty("columns")
        private String[] columns;

        @JsonProperty("columnLabels")
        private Map<String, String> columnLabels;

        @JsonProperty("xAxisLabel")
        private String xAxisLabel;

        @JsonProperty("yAxisLabel")
        private String yAxisLabel;

        @JsonProperty("xAxisField")
        private String xAxisField;

        @JsonProperty("yAxisField")
        private String yAxisField;

        @JsonProperty("seriesFields")
        private String[] seriesFields;

        @JsonProperty("labelField")
        private String labelField;

        @JsonProperty("valueField")
        private String valueField;

        @JsonProperty("xField")
        private String xField;

        @JsonProperty("yField")
        private String yField;

        @JsonProperty("sizeField")
        private String sizeField;

        @JsonProperty("categoryField")
        private String categoryField;

        @JsonProperty("showLegend")
        private boolean showLegend;

        @JsonProperty("showDataLabels")
        private boolean showDataLabels;

        @JsonProperty("legendPosition")
        private String legendPosition;

        @JsonProperty("additionalOptions")
        private Map<String, Object> additionalOptions;
    }
}