package com.planner.dto.request;

import lombok.Data;

@Data
public class DailyReviewRequest {
    private String doneSummary;
    private String undoneSummary;
    private String gains;
    private String improvements;
    private Integer moodScore;
}