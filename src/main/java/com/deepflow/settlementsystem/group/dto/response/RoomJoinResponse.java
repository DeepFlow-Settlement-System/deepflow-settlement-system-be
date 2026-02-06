package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomJoinResponse {
    private Long groupId;
    private String groupName;
    private String message;
}
