package com.planner.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private String status;
    private String priority;
    private Integer plannedMinutes;
    private LocalDate dueAt;
    private LocalDate plannedDate;
    private Long goalId;
    private List<String> tags;
    private List<Long> subTaskIds;
    private List<String> subTaskTitles;
}
