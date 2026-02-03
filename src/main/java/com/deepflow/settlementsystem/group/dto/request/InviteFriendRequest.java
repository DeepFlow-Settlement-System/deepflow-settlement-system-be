package com.deepflow.settlementsystem.group.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InviteFriendRequest {

    @NotEmpty(message = "초대할 친구 UUID 목록은 필수입니다.")
    private List<String> friendUuids; // 카카오 친구 UUID 목록
}
