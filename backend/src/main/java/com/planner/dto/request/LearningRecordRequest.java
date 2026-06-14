package com.planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class LearningRecordRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private Integer durationMinutes;
    private List<String> tags;
    private String source;
}