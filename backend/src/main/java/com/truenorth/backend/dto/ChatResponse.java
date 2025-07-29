package com.truenorth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String query;
    private String visualizationType;
    private String explanation;
    private List<String> columns;
    private String title;
    private String xAxis;
    private String yAxis;
    private List<Map<String, Object>> data;
    private boolean isQueryResponse;
    private String error;
}