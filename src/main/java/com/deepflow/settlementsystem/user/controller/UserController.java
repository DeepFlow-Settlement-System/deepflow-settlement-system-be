package com.deepflow.settlementsystem.user.controller;

import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import com.deepflow.settlementsystem.common.dto.ApiResponse;
import com.deepflow.settlementsystem.user.dto.UserDto;
import com.deepflow.settlementsystem.user.entity.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@SecurityRequirement(name = "Authorization")
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ApiResponseCode.OK, UserDto.fromEntity(user))
        );
    }
}
