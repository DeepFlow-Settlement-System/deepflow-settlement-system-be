package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InviteCodeResponse {
    private String inviteCode;
    private String inviteLink;
    private Long groupId;
    private String groupName;
}
