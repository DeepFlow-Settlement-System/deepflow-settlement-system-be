package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private LocalDateTime joinedAt;
}
