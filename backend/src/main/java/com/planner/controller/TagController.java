package com.planner.controller;

import com.planner.common.Result;
import com.planner.dto.request.TagRequest;
import com.planner.entity.Tag;
import com.planner.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list(@AuthenticationPrincipal UserDetails user) {
        return Result.success(tagService.getTags(getUserId(user.getUsername())));
    }

    @PostMapping
    public Result<Tag> create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TagRequest request) {
        return Result.success(tagService.createTag(getUserId(user.getUsername()), request));
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody TagRequest request) {
        return Result.success(tagService.updateTag(getUserId(user.getUsername()), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        tagService.deleteTag(getUserId(user.getUsername()), id);
        return Result.success();
    }

    private Long getUserId(String username) {
        return 1L;
    }
}