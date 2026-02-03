package com.deepflow.settlementsystem.kakao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class KakaoTokenSaveRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresAt;
}
