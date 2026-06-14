package com.planner.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ProfileAiUpdateResponse {
    private List<SkillUpdate> updates;
    private String summary;

    @Data
    public static class SkillUpdate {
        private String skill;
        private String oldLevel;
        private String newLevel;
        private String reason;
    }
}