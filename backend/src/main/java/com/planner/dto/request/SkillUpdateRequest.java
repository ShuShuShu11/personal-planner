package com.planner.dto.request;

import lombok.Data;

@Data
public class SkillUpdateRequest {
    private String category;
    private String skillName;
    private String level;
    private String keywords;
    private String notes;
}