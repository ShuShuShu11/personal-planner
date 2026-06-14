package com.planner.controller;

import com.planner.common.Result;
import com.planner.dto.request.GoalCreateRequest;
import com.planner.entity.Goal;
import com.planner.entity.KeyResult;
import com.planner.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public Result<List<Goal>> list(@AuthenticationPrincipal UserDetails user) {
        return Result.success(goalService.getGoals(getUserId(user.getUsername())));
    }

    @GetMapping("/tree")
    public Result<List<Goal>> tree(@AuthenticationPrincipal UserDetails user) {
        return Result.success(goalService.getGoalTree(getUserId(user.getUsername())));
    }

    @GetMapping("/active")
    public Result<List<Goal>> active(@AuthenticationPrincipal UserDetails user) {
        return Result.success(goalService.getActiveGoals(getUserId(user.getUsername())));
    }

    @GetMapping("/{id}")
    public Result<Goal> get(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return Result.success(goalService.getGoal(getUserId(user.getUsername()), id));
    }

    @PostMapping
    public Result<Goal> create(@AuthenticationPrincipal UserDetails user, @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.createGoal(getUserId(user.getUsername()), request));
    }

    @PutMapping("/{id}")
    public Result<Goal> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.updateGoal(getUserId(user.getUsername()), id, request));
    }

    @PutMapping("/{id}/progress")
    public Result<Void> progress(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody ProgressRequest request) {
        goalService.updateProgress(getUserId(user.getUsername()), id, request.getProgress());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        goalService.deleteGoal(getUserId(user.getUsername()), id);
        return Result.success();
    }

    @GetMapping("/{id}/key-results")
    public Result<List<KeyResult>> keyResults(@PathVariable Long id) {
        return Result.success(goalService.getKeyResults(id));
    }

    @PostMapping("/{id}/key-results")
    public Result<KeyResult> createKeyResult(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody KeyResultRequest request) {
        return Result.success(goalService.createKeyResult(getUserId(user.getUsername()), id, request.getTitle(), request.getTargetValue(), request.getUnit()));
    }

    @PutMapping("/{goalId}/key-results/{krId}")
    public Result<Void> updateKeyResult(@AuthenticationPrincipal UserDetails user, @PathVariable Long goalId, @PathVariable Long krId, @RequestBody KeyResultUpdateRequest request) {
        goalService.updateKeyResult(getUserId(user.getUsername()), goalId, krId, request.getCurrentValue());
        return Result.success();
    }

    private Long getUserId(String username) {
        return 1L;
    }

    @lombok.Data
    public static class ProgressRequest {
        private int progress;
    }

    @lombok.Data
    public static class KeyResultRequest {
        private String title;
        private Double targetValue;
        private String unit;
    }

    @lombok.Data
    public static class KeyResultUpdateRequest {
        private Double currentValue;
    }
}