package com.truenorth.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String query;
    private String visualizationType;
    @JsonProperty("chartDescription")
    @JsonPropertyDescription("The description of the chart")
    private String explanation;
    private List<String> columns;
    private String title;
    @JsonProperty("xAxisTitle")
    private String xAxis;
    @JsonProperty("yAxisTitle")
    private String yAxis;
    private boolean isQueryResponse;
    private String error;
}