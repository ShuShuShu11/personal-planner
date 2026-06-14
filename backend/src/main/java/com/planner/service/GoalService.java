package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.planner.common.BusinessException;
import com.planner.dto.request.GoalCreateRequest;
import com.planner.entity.Goal;
import com.planner.entity.KeyResult;
import com.planner.mapper.GoalMapper;
import com.planner.mapper.KeyResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalMapper goalMapper;
    private final KeyResultMapper keyResultMapper;

    public List<Goal> getGoals(Long userId) {
        return goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .orderByAsc(Goal::getLevel, Goal::getCreatedAt));
    }

    public List<Goal> getActiveGoals(Long userId) {
        return goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .eq(Goal::getStatus, "active")
                .orderByAsc(Goal::getCreatedAt));
    }

    public List<Goal> getGoalTree(Long userId) {
        List<Goal> all = goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .orderByAsc(Goal::getLevel, Goal::getCreatedAt));
        return buildTree(all, null);
    }

    private List<Goal> buildTree(List<Goal> all, Long parentId) {
        List<Goal> result = new ArrayList<>();
        for (Goal g : all) {
            if ((parentId == null && g.getParentId() == null) || (parentId != null && parentId.equals(g.getParentId()))) {
                result.add(g);
            }
        }
        return result;
    }

    public Goal getGoal(Long userId, Long goalId) {
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new BusinessException("目标不存在");
        }
        return goal;
    }

    public Goal createGoal(Long userId, GoalCreateRequest req) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(req.getTitle());
        goal.setDescription(req.getDescription());
        goal.setLevel(req.getLevel() != null ? req.getLevel() : "month");
        goal.setParentId(req.getParentId());
        goal.setStatus("active");
        goal.setProgress(0);
        goal.setStartDate(req.getStartDate());
        goal.setEndDate(req.getEndDate());
        goalMapper.insert(goal);
        return goal;
    }

    public Goal updateGoal(Long userId, Long goalId, GoalCreateRequest req) {
        Goal goal = getGoal(userId, goalId);
        if (req.getTitle() != null) goal.setTitle(req.getTitle());
        if (req.getDescription() != null) goal.setDescription(req.getDescription());
        if (req.getLevel() != null) goal.setLevel(req.getLevel());
        if (req.getStartDate() != null) goal.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) goal.setEndDate(req.getEndDate());
        goalMapper.updateById(goal);
        return goal;
    }

    @Transactional
    public void updateProgress(Long userId, Long goalId, int progress) {
        Goal goal = getGoal(userId, goalId);
        goal.setProgress(Math.min(100, Math.max(0, progress)));
        goalMapper.updateById(goal);
    }

    public void deleteGoal(Long userId, Long goalId) {
        getGoal(userId, goalId);
        goalMapper.deleteById(goalId);
    }

    public List<KeyResult> getKeyResults(Long goalId) {
        return keyResultMapper.selectList(new LambdaQueryWrapper<KeyResult>().eq(KeyResult::getGoalId, goalId));
    }

    @Transactional
    public KeyResult createKeyResult(Long userId, Long goalId, String title, Double targetValue, String unit) {
        getGoal(userId, goalId);
        KeyResult kr = new KeyResult();
        kr.setGoalId(goalId);
        kr.setTitle(title);
        kr.setTargetValue(targetValue);
        kr.setCurrentValue(0.0);
        kr.setUnit(unit);
        keyResultMapper.insert(kr);
        return kr;
    }

    @Transactional
    public void updateKeyResult(Long userId, Long goalId, Long krId, Double currentValue) {
        getGoal(userId, goalId);
        KeyResult kr = keyResultMapper.selectById(krId);
        if (kr == null || !kr.getGoalId().equals(goalId)) {
            throw new BusinessException("关键结果不存在");
        }
        kr.setCurrentValue(currentValue);
        keyResultMapper.updateById(kr);
        updateGoalProgressFromKeyResults(userId, goalId);
    }

    private void updateGoalProgressFromKeyResults(Long userId, Long goalId) {
        List<KeyResult> krs = getKeyResults(goalId);
        if (krs.isEmpty()) return;
        double totalProgress = krs.stream()
                .mapToDouble(kr -> kr.getTargetValue() > 0 ? (kr.getCurrentValue() / kr.getTargetValue()) * 100 : 0)
                .average().orElse(0);
        updateProgress(userId, goalId, (int) totalProgress);
    }
}