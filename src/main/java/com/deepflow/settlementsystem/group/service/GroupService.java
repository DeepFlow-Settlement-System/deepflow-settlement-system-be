package com.deepflow.settlementsystem.group.service;

import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.group.dto.request.GroupCreateRequest;
import com.deepflow.settlementsystem.group.dto.response.*;
import com.deepflow.settlementsystem.group.entity.Group;
import com.deepflow.settlementsystem.group.entity.Member;
import com.deepflow.settlementsystem.group.entity.Room;
import com.deepflow.settlementsystem.group.repository.GroupRepository;
import com.deepflow.settlementsystem.group.repository.MemberRepository;
import com.deepflow.settlementsystem.group.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

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

        // 방 생성
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
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), userId);
        if (!isMember) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
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

    public InviteCodeResponse getInviteCode(Long groupId, Long userId) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), userId);
        if (!isMember) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }

        return InviteCodeResponse.builder()
                .inviteCode(group.getRoom().getInviteCode())
                .inviteLink(generateInviteLink(group.getRoom().getInviteCode()))
                .groupId(group.getId())
                .groupName(group.getName())
                .build();
    }

    // 초대 코드로 그룹 정보 조회 (인증 불필요)
    public GroupJoinInfoResponse getJoinInfo(String inviteCode) {
        Room room = roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        // 만료 체크
        if (room.isExpired()) {
            throw new CustomException(ErrorCode.INVITE_CODE_EXPIRED);
        }

        return GroupJoinInfoResponse.builder()
                .groupId(room.getGroup().getId())
                .groupName(room.getGroup().getName())
                .groupDescription(room.getGroup().getDescription())
                .inviteCode(inviteCode)
                .build();
    }

    // 그룹 참여 (인증 선택적)
    @Transactional
    public RoomJoinResponse joinRoom(String inviteCode, Long userId) {
        Room room = roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        // 만료 체크
        if (room.isExpired()) {
            throw new CustomException(ErrorCode.INVITE_CODE_EXPIRED);
        }

        // userId가 null이면 (비로그인) 예외 발생
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 이미 멤버인지 확인
        if (memberRepository.existsByRoomIdAndUserId(room.getId(), userId)) {
            throw new CustomException(ErrorCode.ALREADY_MEMBER);
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
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        Member member = memberRepository.findByRoomIdAndUserId(group.getRoom().getId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_GROUP_MEMBER));

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

    private String generateInviteLink(String inviteCode) {
        return baseUrl + "/api/groups/join?code=" + inviteCode;
    }
}
