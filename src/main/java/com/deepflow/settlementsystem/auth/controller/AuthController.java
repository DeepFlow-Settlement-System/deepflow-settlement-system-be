package com.deepflow.settlementsystem.auth.controller;import com.deepflow.settlementsystem.auth.dto.KakaoLoginUrlResponse;
import com.deepflow.settlementsystem.auth.dto.LoginResponse;
import com.deepflow.settlementsystem.auth.service.AuthService;
import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import com.deepflow.settlementsystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/v1/oauth2/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @RequestParam String code
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, authService.kakaoLogin(code, false))
        );
    }

    @GetMapping("/dev/v1/oauth2/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoDevLogin(
            @RequestParam String code
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, authService.kakaoLogin(code, true))
        );
    }
}
