package com.truenorth.backend.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after processing a natural language query into a SQL result with visualization metadata.")
public class QueryResponse {

    @Schema(description = "The SQL query generated from the user's message.", example = "SELECT city, COUNT(*) FROM klupica.autoinsurance GROUP BY city")
    private String query;

    @Schema(description = "Type of chart suitable for the result: bar, pie, line, scatter, table, or card.", example = "bar")
    private String visualizationType;

    @Schema(description = "Explanation of what the query shows, written in natural language.", example = "Displays number of customers by city.")
    private String explanation;

    @Schema(description = "List of column names returned by the query.", example = "[\"city\", \"customer_count\"]")
    private List<String> columns;

    @Schema(description = "Title to be used in the chart or data card.", example = "Customers per City")
    private String title;

    @Schema(description = "Name of the column to be used as the X axis in a chart.", example = "city")
    private String xAxis;

    @Schema(description = "Name of the column to be used as the Y axis in a chart.", example = "customer_count")
    private String yAxis;

    @ArraySchema(
            schema = @Schema(
                    type = "object",
                    description = "Query result rows; each item is a row represented as a key-value map.",
                    example = "{\"city\": \"Zagreb\", \"customer_count\": 120}"
            )
    )
    private List<Map<String, Object>> data;

    @Schema(description = "True if this response contains a valid query + data; false if it's a fallback/chat message.", example = "true")
    private boolean isQueryResponse;

    @Schema(description = "Error message if something went wrong during query generation or execution.", example = "Invalid column: 'foobar'")
    private String error;
}