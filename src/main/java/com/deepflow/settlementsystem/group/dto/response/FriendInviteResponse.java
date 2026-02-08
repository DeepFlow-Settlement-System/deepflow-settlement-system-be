package com.deepflow.settlementsystem.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FriendInviteResponse {
    private Long groupId;
    private String groupName;
    private List<InvitedFriend> invitedFriends;
    private List<String> notFoundFriends; // 카카오 UUID로 User를 찾지 못한 친구들

    @Getter
    @Builder
    public static class InvitedFriend {
        private Long userId;
        private String kakaoUuid;
        private String name;
    }
}
