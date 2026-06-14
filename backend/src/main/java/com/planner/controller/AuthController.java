package com.planner.controller;

import com.planner.common.Result;
import com.planner.dto.request.LoginRequest;
import com.planner.dto.request.RegisterRequest;
import com.planner.dto.response.LoginResponse;
import com.planner.dto.response.UserInfo;
import com.planner.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public Result<UserInfo> me(@AuthenticationPrincipal UserDetails user) {
        Long userId = authService.getUserIdByUsername(user.getUsername());
        return Result.success(authService.me(userId));
    }

    @lombok.Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}