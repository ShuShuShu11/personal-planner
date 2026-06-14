package com.planner.dto.request;

import lombok.Data;

@Data
public class ProfileDocumentUpdateRequest {
    private String markdown;
    private String sourceMd;
    private Boolean regenerate;
    private Boolean includeLearning;
}
