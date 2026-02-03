package com.deepflow.settlementsystem.auth;

import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import com.deepflow.settlementsystem.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, response)
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        authService.signUp(request);

        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, null)
        );
    }
}
