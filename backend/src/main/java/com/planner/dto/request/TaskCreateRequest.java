package com.planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    private String priority;
    private Integer plannedMinutes;
    private LocalDate dueAt;
    private LocalDate plannedDate;
    private Long goalId;
    private List<String> tags;
    private List<String> subTaskTitles;
}
