package com.deepflow.settlementsystem.group.service;

import com.deepflow.settlementsystem.group.dto.request.GroupCreateRequest;
import com.deepflow.settlementsystem.group.dto.request.RoomJoinRequest;
import com.deepflow.settlementsystem.group.dto.response.FriendInviteResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupDetailResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupResponse;
import com.deepflow.settlementsystem.group.dto.response.MemberResponse;
import com.deepflow.settlementsystem.group.dto.response.RoomJoinResponse;
import com.deepflow.settlementsystem.group.entity.Group;
import com.deepflow.settlementsystem.group.entity.Member;
import com.deepflow.settlementsystem.group.entity.Room;
import com.deepflow.settlementsystem.group.repository.GroupRepository;
import com.deepflow.settlementsystem.group.repository.MemberRepository;
import com.deepflow.settlementsystem.group.repository.RoomRepository;
import com.deepflow.settlementsystem.kakao.dto.response.KakaoFriendResponse;
import com.deepflow.settlementsystem.kakao.service.KakaoService;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final KakaoService kakaoService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, Long userId) {
        // 그룹 생성
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        group = groupRepository.save(group);

        // 방 생성 (1:1 관계)
        Room room = Room.builder()
                .group(group)
                .build();
        room = roomRepository.save(room);
        group.setRoom(room);

        // 첫 번째 멤버로 추가
        Member firstMember = Member.builder()
                .room(room)
                .userId(userId)
                .build();
        memberRepository.save(firstMember);

        return toGroupResponse(group, room);
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        List<Group> groups = groupRepository.findAllByUserId(userId);
        return groups.stream()
                .map(group -> toGroupResponse(group, group.getRoom()))
                .collect(Collectors.toList());
    }

    public GroupDetailResponse getGroupDetail(Long groupId, Long userId) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), userId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹에 접근할 권한이 없습니다.");
        }

        // 멤버 목록 조회
        List<Member> members = memberRepository.findByRoomId(group.getRoom().getId());
        List<MemberResponse> memberResponses = members.stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .userId(member.getUserId())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .inviteCode(group.getRoom().getInviteCode())
                .inviteLink(generateInviteLink(group.getRoom().getInviteCode()))
                .members(memberResponses)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    @Transactional
    public RoomJoinResponse joinRoom(RoomJoinRequest request, Long userId) {
        Room room = roomRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // 이미 멤버인지 확인
        if (memberRepository.existsByRoomIdAndUserId(room.getId(), userId)) {
            throw new IllegalArgumentException("이미 해당 그룹의 멤버입니다.");
        }

        // 멤버 추가
        Member member = Member.builder()
                .room(room)
                .userId(userId)
                .build();
        memberRepository.save(member);

        return RoomJoinResponse.builder()
                .groupId(room.getGroup().getId())
                .groupName(room.getGroup().getName())
                .message("그룹에 성공적으로 참여했습니다.")
                .build();
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        Member member = memberRepository.findByRoomIdAndUserId(group.getRoom().getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 멤버가 아닙니다."));

        memberRepository.delete(member);

        // 모든 인원이 나갔는지 확인
        List<Member> remainingMembers = memberRepository.findByRoomId(group.getRoom().getId());
        if (remainingMembers.isEmpty()) {
            // 그룹과 방 삭제 (Cascade로 인해 Room도 함께 삭제됨)
            groupRepository.delete(group);
        }
    }

    private GroupResponse toGroupResponse(Group group, Room room) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .inviteCode(room.getInviteCode())
                .inviteLink(generateInviteLink(room.getInviteCode()))
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    public List<KakaoFriendResponse> getFriends(Long userId) {
        return kakaoService.getFriends(userId);
    }

    @Transactional
    public FriendInviteResponse inviteFriends(Long groupId, List<String> friendUuids, Long inviterUserId) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 초대하는 사람이 그룹 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), inviterUserId);
        if (!isMember) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }

        List<FriendInviteResponse.InvitedFriend> invitedFriends = new ArrayList<>();
        List<String> notFoundFriends = new ArrayList<>();

        for (String friendUuid : friendUuids) {
            // 카카오 UUID로 User 조회
            User user = userRepository.findByKakaoUuid(friendUuid)
                    .orElse(null);

            if (user == null) {
                notFoundFriends.add(friendUuid);
                continue;
            }

            // 이미 멤버인지 확인
            if (memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), user.getId())) {
                continue; // 이미 멤버면 스킵
            }

            // 멤버 추가
            Member member = Member.builder()
                    .room(group.getRoom())
                    .userId(user.getId())
                    .build();
            memberRepository.save(member);

            invitedFriends.add(FriendInviteResponse.InvitedFriend.builder()
                    .userId(user.getId())
                    .kakaoUuid(friendUuid)
                    .name(user.getName())
                    .build());
        }

        return FriendInviteResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .invitedFriends(invitedFriends)
                .notFoundFriends(notFoundFriends)
                .build();
    }

    private String generateInviteLink(String inviteCode) {
        return baseUrl + "/api/groups/join?code=" + inviteCode;
    }
}
