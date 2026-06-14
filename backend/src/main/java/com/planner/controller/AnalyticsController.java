package com.planner.controller;

import com.planner.common.Result;
import com.planner.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> heatmap(@AuthenticationPrincipal UserDetails user, @RequestParam(defaultValue = "90") int days) {
        return Result.success(analyticsService.getHeatmap(getUserId(user.getUsername()), days));
    }

    @GetMapping("/time-by-tag")
    public Result<Map<String, Object>> timeByTag(@AuthenticationPrincipal UserDetails user, @RequestParam(defaultValue = "30") int days) {
        return Result.success(analyticsService.getTimeByTag(getUserId(user.getUsername()), days));
    }

    @GetMapping("/completion-trend")
    public Result<List<Map<String, Object>>> completionTrend(@AuthenticationPrincipal UserDetails user, @RequestParam(defaultValue = "30") int days) {
        return Result.success(analyticsService.getCompletionTrend(getUserId(user.getUsername()), days));
    }

    @GetMapping("/today")
    public Result<Map<String, Object>> today(@AuthenticationPrincipal UserDetails user) {
        return Result.success(analyticsService.getTodayOverview(getUserId(user.getUsername())));
    }

    @GetMapping("/range")
    public Result<Map<String, Object>> range(@AuthenticationPrincipal UserDetails user, @RequestParam(defaultValue = "30") int days) {
        return Result.success(analyticsService.getRangeOverview(getUserId(user.getUsername()), days));
    }

    private Long getUserId(String username) {
        return 1L;
    }
}