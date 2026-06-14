package com.planner.dto.request;

import lombok.Data;

@Data
public class ProfileMdGenerateRequest {
    private String mdContent;
    private Boolean includeLearning;
}
