package com.deepflow.settlementsystem.auth.controller;

import com.deepflow.settlementsystem.auth.dto.KakaoLoginRequest;
import com.deepflow.settlementsystem.auth.dto.KakaoLoginUrlResponse;
import com.deepflow.settlementsystem.auth.dto.LoginResponse;
import com.deepflow.settlementsystem.auth.service.AuthService;
import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import com.deepflow.settlementsystem.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login-url/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginUrlResponse>> getKakaoLoginUrl() {
        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, authService.getKakaoLoginUrl())
        );
    }

    @PostMapping("/login/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @RequestBody @Valid KakaoLoginRequest kakaoLoginRequest
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, authService.kakaoLogin(kakaoLoginRequest))
        );
    }
}
