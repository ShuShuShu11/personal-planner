package com.planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class GoalCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    private String level;
    private Long parentId;
    private LocalDate startDate;
    private LocalDate endDate;
}