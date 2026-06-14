package com.planner.controller;

import com.planner.common.Result;
import com.planner.dto.request.DailyReviewRequest;
import com.planner.entity.DailyReview;
import com.planner.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/today")
    public Result<DailyReview> today(@AuthenticationPrincipal UserDetails user) {
        return Result.success(reviewService.getOrCreateTodayReview(getUserId(user.getUsername())));
    }

    @GetMapping
    public Result<List<DailyReview>> list(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        return Result.success(reviewService.getReviews(getUserId(user.getUsername()), start, end));
    }

    @PutMapping("/today")
    public Result<DailyReview> updateToday(@AuthenticationPrincipal UserDetails user, @RequestBody DailyReviewRequest request) {
        return Result.success(reviewService.updateTodayReview(getUserId(user.getUsername()), request));
    }

    @GetMapping("/week")
    public Result<Map<String, Object>> week(@AuthenticationPrincipal UserDetails user) {
        return Result.success(reviewService.getWeekReview(getUserId(user.getUsername())));
    }

    @GetMapping("/month")
    public Result<Map<String, Object>> month(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "6") int month) {
        return Result.success(reviewService.getMonthReview(getUserId(user.getUsername()), year, month));
    }

    private Long getUserId(String username) {
        return 1L;
    }
}