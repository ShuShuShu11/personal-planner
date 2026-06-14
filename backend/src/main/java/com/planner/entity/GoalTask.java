package com.planner.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoalTask {
    private Long goalId;
    private Long taskId;
    private Integer contribution;
}