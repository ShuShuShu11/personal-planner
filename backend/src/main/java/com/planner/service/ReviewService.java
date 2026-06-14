package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.planner.common.BusinessException;
import com.planner.dto.request.DailyReviewRequest;
import com.planner.entity.DailyReview;
import com.planner.entity.Task;
import com.planner.mapper.DailyReviewMapper;
import com.planner.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final DailyReviewMapper reviewMapper;
    private final TaskMapper taskMapper;

    public DailyReview getTodayReview(Long userId) {
        return reviewMapper.selectOne(new LambdaQueryWrapper<DailyReview>()
                .eq(DailyReview::getUserId, userId)
                .eq(DailyReview::getReviewDate, LocalDate.now()));
    }

    public DailyReview getOrCreateTodayReview(Long userId) {
        DailyReview review = getTodayReview(userId);
        if (review == null) {
            review = new DailyReview();
            review.setUserId(userId);
            review.setReviewDate(LocalDate.now());
            reviewMapper.insert(review);
        }
        return review;
    }

    public DailyReview updateTodayReview(Long userId, DailyReviewRequest req) {
        DailyReview review = getOrCreateTodayReview(userId);
        if (req.getDoneSummary() != null) review.setDoneSummary(req.getDoneSummary());
        if (req.getUndoneSummary() != null) review.setUndoneSummary(req.getUndoneSummary());
        if (req.getGains() != null) review.setGains(req.getGains());
        if (req.getImprovements() != null) review.setImprovements(req.getImprovements());
        if (req.getMoodScore() != null) review.setMoodScore(req.getMoodScore());
        reviewMapper.updateById(review);
        return review;
    }

    public List<DailyReview> getReviews(Long userId, LocalDate start, LocalDate end) {
        return reviewMapper.selectList(new LambdaQueryWrapper<DailyReview>()
                .eq(DailyReview::getUserId, userId)
                .ge(start != null, DailyReview::getReviewDate, start)
                .le(end != null, DailyReview::getReviewDate, end)
                .orderByDesc(DailyReview::getReviewDate));
    }

    public Map<String, Object> getWeekReview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<DailyReview> reviews = reviewMapper.selectList(new LambdaQueryWrapper<DailyReview>()
                .eq(DailyReview::getUserId, userId)
                .ge(DailyReview::getReviewDate, weekStart)
                .le(DailyReview::getReviewDate, weekEnd)
                .orderByAsc(DailyReview::getReviewDate));
        List<Task> doneTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, "done")
                .ge(Task::getPlannedDate, weekStart)
                .le(Task::getPlannedDate, weekEnd));
        long totalPlanned = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ge(Task::getPlannedDate, weekStart)
                .le(Task::getPlannedDate, weekEnd));
        double completionRate = totalPlanned > 0 ? (doneTasks.size() * 100.0 / totalPlanned) : 0;
        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("doneCount", doneTasks.size());
        result.put("totalPlanned", totalPlanned);
        result.put("completionRate", completionRate);
        return result;
    }

    public Map<String, Object> getMonthReview(Long userId, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        List<DailyReview> reviews = reviewMapper.selectList(new LambdaQueryWrapper<DailyReview>()
                .eq(DailyReview::getUserId, userId)
                .ge(DailyReview::getReviewDate, monthStart)
                .le(DailyReview::getReviewDate, monthEnd)
                .orderByAsc(DailyReview::getReviewDate));
        List<Task> doneTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, "done")
                .ge(Task::getPlannedDate, monthStart)
                .le(Task::getPlannedDate, monthEnd));
        long totalPlanned = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ge(Task::getPlannedDate, monthStart)
                .le(Task::getPlannedDate, monthEnd));
        double completionRate = totalPlanned > 0 ? (doneTasks.size() * 100.0 / totalPlanned) : 0;
        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("doneCount", doneTasks.size());
        result.put("totalPlanned", totalPlanned);
        result.put("completionRate", completionRate);
        return result;
    }
}