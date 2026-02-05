package com.deepflow.settlementsystem.user.controller;

import com.deepflow.settlementsystem.auth.dto.SignUpRequest;
import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import com.deepflow.settlementsystem.common.dto.ApiResponse;
import com.deepflow.settlementsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        userService.signUp(request);

        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, null)
        );
    }
}
