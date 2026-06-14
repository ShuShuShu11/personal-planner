package com.planner.controller;

import com.planner.common.Result;
import com.planner.dto.request.ProfileAiUpdateRequest;
import com.planner.dto.request.ProfileDocumentUpdateRequest;
import com.planner.dto.request.ProfileMdGenerateRequest;
import com.planner.dto.request.SkillUpdateRequest;
import com.planner.dto.response.ProfileAiUpdateResponse;
import com.planner.dto.response.ProfileMdResponse;
import com.planner.entity.ProfileChangeHistory;
import com.planner.entity.ProfileDocument;
import com.planner.entity.SkillProfile;
import com.planner.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public Result<List<SkillProfile>> profile(@AuthenticationPrincipal UserDetails user) {
        return Result.success(profileService.getProfile(getUserId(user.getUsername())));
    }

    @PostMapping("/skills")
    public Result<SkillProfile> addSkill(@AuthenticationPrincipal UserDetails user, @RequestBody SkillUpdateRequest request) {
        return Result.success(profileService.addSkill(getUserId(user.getUsername()), request));
    }

    @PutMapping("/skills/{id}")
    public Result<SkillProfile> updateSkill(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody SkillUpdateRequest request) {
        return Result.success(profileService.updateSkill(getUserId(user.getUsername()), id, request));
    }

    @GetMapping("/history")
    public Result<List<ProfileChangeHistory>> history(@AuthenticationPrincipal UserDetails user) {
        return Result.success(profileService.getHistory(getUserId(user.getUsername())));
    }

    @PostMapping("/ai-update")
    public Result<ProfileAiUpdateResponse> aiUpdate(@AuthenticationPrincipal UserDetails user, @RequestBody ProfileAiUpdateRequest request) {
        return Result.success(profileService.aiUpdate(getUserId(user.getUsername()), request));
    }

    @PostMapping("/ai-generate-md")
    public Result<ProfileMdResponse> aiGenerateMd(@AuthenticationPrincipal UserDetails user, @RequestBody ProfileMdGenerateRequest request) {
        return Result.success(profileService.saveOrUpdateProfileDocument(getUserId(user.getUsername()),
                toUpdateRequest(request, true)));
    }

    @GetMapping("/document")
    public Result<ProfileDocument> getDocument(@AuthenticationPrincipal UserDetails user) {
        return Result.success(profileService.getProfileDocument(getUserId(user.getUsername())));
    }

    @PutMapping("/document")
    public Result<ProfileMdResponse> updateDocument(@AuthenticationPrincipal UserDetails user, @RequestBody ProfileDocumentUpdateRequest request) {
        return Result.success(profileService.saveOrUpdateProfileDocument(getUserId(user.getUsername()), request));
    }

    private ProfileDocumentUpdateRequest toUpdateRequest(ProfileMdGenerateRequest src, boolean regenerate) {
        ProfileDocumentUpdateRequest r = new ProfileDocumentUpdateRequest();
        r.setSourceMd(src.getMdContent());
        r.setIncludeLearning(src.getIncludeLearning());
        r.setRegenerate(regenerate);
        return r;
    }

    private Long getUserId(String username) {
        return 1L;
    }
}
