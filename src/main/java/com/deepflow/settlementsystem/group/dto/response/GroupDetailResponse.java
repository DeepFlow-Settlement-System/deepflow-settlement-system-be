package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String inviteCode;
    private String inviteLink;
    private List<MemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
