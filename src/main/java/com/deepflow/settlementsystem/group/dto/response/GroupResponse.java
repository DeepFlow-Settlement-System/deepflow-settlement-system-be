package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String inviteCode;
    private String inviteLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
