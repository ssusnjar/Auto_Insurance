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
    private String explanation;
    private List<String> columns;
    private List<Map<String, Object>> data;
    private String title;
    private String xAxis;
    private String yAxis;
    private boolean isQueryResponse;
    private boolean error;
}
