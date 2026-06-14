package com.planner.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ProfileMdResponse {
    private String markdown;
    private List<String> suggestedSkills;
}
