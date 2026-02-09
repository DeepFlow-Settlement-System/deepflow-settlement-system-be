package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GroupJoinInfoResponse {
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String inviteCode;
}
