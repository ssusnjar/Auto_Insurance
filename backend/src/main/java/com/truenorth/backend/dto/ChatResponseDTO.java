package com.truenorth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String visualizationType;
    private ChartConfigDTO chartConfig;
    private String explanation;

    private List<Map<String, Object>> data;
    private DataSummary summary;

    private boolean isValid;
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartConfigDTO {
        private String title;
        private String subtitle;

        private List<String> columns;
        private Map<String, String> columnLabels;

        private String xAxisLabel;
        private String yAxisLabel;
        private String xAxisField;
        private String yAxisField;
        private List<String> seriesFields;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSummary {
        private Long totalRecords;
        private Double total;
        private Double average;
        private Double min;
        private Double max;
        private Map<String, Object> additionalStats;
    }
}