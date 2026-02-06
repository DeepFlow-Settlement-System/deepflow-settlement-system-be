package com.deepflow.settlementsystem.group.service;

import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.group.dto.request.GroupCreateRequest;
import com.deepflow.settlementsystem.group.dto.response.GroupDetailResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupJoinInfoResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupResponse;
import com.deepflow.settlementsystem.group.dto.response.InviteCodeResponse;
import com.deepflow.settlementsystem.group.dto.response.RoomJoinResponse;
import com.deepflow.settlementsystem.group.entity.Group;
import com.deepflow.settlementsystem.group.entity.Member;
import com.deepflow.settlementsystem.group.entity.Room;
import com.deepflow.settlementsystem.group.repository.GroupRepository;
import com.deepflow.settlementsystem.group.repository.MemberRepository;
import com.deepflow.settlementsystem.group.repository.RoomRepository;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("GroupService 테스트")
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = createTestUser(1L, "user1");
        testUser2 = createTestUser(2L, "user2");
    }

    private User createTestUser(Long kakaoId, String nickname) {
        return userRepository.save(User.builder()
                .kakaoId(kakaoId)
                .username(UUID.randomUUID().toString())
                .nickname(nickname)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build());
    }

    private GroupCreateRequest createGroupRequest(String name, String description) {
        GroupCreateRequest request = new GroupCreateRequest();
        try {
            Field nameField = GroupCreateRequest.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(request, name);
            
            if (description != null) {
                Field descField = GroupCreateRequest.class.getDeclaredField("description");
                descField.setAccessible(true);
                descField.set(request, description);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GroupCreateRequest", e);
        }
        return request;
    }

    @Test
    @DisplayName("그룹 생성 성공 - Group, Room, Member가 모두 생성되어야 함")
    void createGroup_Success() {
        // given
        GroupCreateRequest request = createGroupRequest("테스트 그룹", "테스트 설명");

        // when
        GroupResponse response = groupService.createGroup(request, testUser1);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("테스트 그룹");
        assertThat(response.getDescription()).isEqualTo("테스트 설명");
        assertThat(response.getInviteCode()).isNotBlank();
        assertThat(response.getInviteLink()).isNotBlank();

        // Group 확인
        Group savedGroup = groupRepository.findById(response.getId()).orElseThrow();
        assertThat(savedGroup.getName()).isEqualTo("테스트 그룹");
        assertThat(savedGroup.getRoom()).isNotNull();

        // Room 확인
        Room savedRoom = savedGroup.getRoom();
        assertThat(savedRoom.getInviteCode()).isNotBlank();
        assertThat(savedRoom.getExpiresAt()).isNotNull();

        // Member 확인
        List<Member> members = memberRepository.findByRoomId(savedRoom.getId());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUser().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("내 그룹 목록 조회 성공")
    void getMyGroups_Success() {
        // given
        groupService.createGroup(createGroupRequest("그룹 1", null), testUser1);
        groupService.createGroup(createGroupRequest("그룹 2", null), testUser1);

        // 다른 사용자의 그룹
        groupService.createGroup(createGroupRequest("그룹 3", null), testUser2);

        // when
        List<GroupResponse> myGroups = groupService.getMyGroups(testUser1);

        // then
        assertThat(myGroups).hasSize(2);
        assertThat(myGroups).extracting(GroupResponse::getName)
                .containsExactlyInAnyOrder("그룹 1", "그룹 2");
    }

    @Test
    @DisplayName("내 그룹 목록 조회 - 참여한 그룹이 없을 때 빈 목록 반환")
    void getMyGroups_EmptyList() {
        // when
        List<GroupResponse> myGroups = groupService.getMyGroups(testUser1);

        // then
        assertThat(myGroups).isEmpty();
    }

    @Test
    @DisplayName("그룹 상세 조회 성공")
    void getGroupDetail_Success() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", "테스트 설명"), testUser1);

        // when
        GroupDetailResponse response = groupService.getGroupDetail(createdGroup.getId(), testUser1);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(createdGroup.getId());
        assertThat(response.getName()).isEqualTo("테스트 그룹");
        assertThat(response.getDescription()).isEqualTo("테스트 설명");
        assertThat(response.getInviteCode()).isNotBlank();
        assertThat(response.getInviteLink()).isNotBlank();
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getUserId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("그룹 상세 조회 실패 - 그룹이 존재하지 않음")
    void getGroupDetail_GroupNotFound() {
        // when & then
        assertThatThrownBy(() -> groupService.getGroupDetail(999L, testUser1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    @DisplayName("그룹 상세 조회 실패 - 그룹 멤버가 아님")
    void getGroupDetail_NoAccessPermission() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);

        // when & then
        assertThatThrownBy(() -> groupService.getGroupDetail(createdGroup.getId(), testUser2))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NO_ACCESS_PERMISSION);
    }

    @Test
    @DisplayName("초대 코드 조회 성공")
    void getInviteCode_Success() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);

        // when
        InviteCodeResponse response = groupService.getInviteCode(createdGroup.getId(), testUser1);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInviteCode()).isNotBlank();
        assertThat(response.getInviteLink()).isNotBlank();
        assertThat(response.getGroupId()).isEqualTo(createdGroup.getId());
        assertThat(response.getGroupName()).isEqualTo("테스트 그룹");
    }

    @Test
    @DisplayName("초대 코드 조회 실패 - 그룹 멤버가 아님")
    void getInviteCode_NoAccessPermission() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);

        // when & then
        assertThatThrownBy(() -> groupService.getInviteCode(createdGroup.getId(), testUser2))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NO_ACCESS_PERMISSION);
    }

    @Test
    @DisplayName("초대 코드로 그룹 정보 조회 성공")
    void getJoinInfo_Success() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", "테스트 설명"), testUser1);
        
        Group group = groupRepository.findById(createdGroup.getId()).orElseThrow();
        String inviteCode = group.getRoom().getInviteCode();

        // when
        GroupJoinInfoResponse response = groupService.getJoinInfo(inviteCode);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(createdGroup.getId());
        assertThat(response.getGroupName()).isEqualTo("테스트 그룹");
        assertThat(response.getGroupDescription()).isEqualTo("테스트 설명");
        assertThat(response.getInviteCode()).isEqualTo(inviteCode);
    }

    @Test
    @DisplayName("초대 코드로 그룹 정보 조회 실패 - 유효하지 않은 초대 코드")
    void getJoinInfo_InvalidInviteCode() {
        // when & then
        assertThatThrownBy(() -> groupService.getJoinInfo("invalid_code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("초대 코드로 그룹 정보 조회 실패 - 만료된 초대 코드")
    void getJoinInfo_ExpiredInviteCode() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Group group = groupRepository.findById(createdGroup.getId()).orElseThrow();
        Room room = group.getRoom();
        
        // 만료 시간을 과거로 설정 (리플렉션 사용)
        try {
            Field expiresAtField = Room.class.getDeclaredField("expiresAt");
            expiresAtField.setAccessible(true);
            expiresAtField.set(room, java.time.LocalDateTime.now().minusDays(1));
            roomRepository.save(room);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set expired time", e);
        }
        
        String inviteCode = room.getInviteCode();

        // when & then
        assertThatThrownBy(() -> groupService.getJoinInfo(inviteCode))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVITE_CODE_EXPIRED);
    }

    @Test
    @DisplayName("그룹 참여 성공")
    void joinRoom_Success() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Group group = groupRepository.findById(createdGroup.getId()).orElseThrow();
        String inviteCode = group.getRoom().getInviteCode();

        // when
        RoomJoinResponse response = groupService.joinRoom(inviteCode, testUser2);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(createdGroup.getId());
        assertThat(response.getGroupName()).isEqualTo("테스트 그룹");
        assertThat(response.getMessage()).isEqualTo("그룹에 성공적으로 참여했습니다.");

        // Member 확인
        List<Member> members = memberRepository.findByRoomId(group.getRoom().getId());
        assertThat(members).hasSize(2);
        assertThat(members).extracting(m -> m.getUser().getId())
                .containsExactlyInAnyOrder(testUser1.getId(), testUser2.getId());
    }

    @Test
    @DisplayName("그룹 참여 실패 - 유효하지 않은 초대 코드")
    void joinRoom_InvalidInviteCode() {
        // when & then
        assertThatThrownBy(() -> groupService.joinRoom("invalid_code", testUser1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("그룹 참여 실패 - 비로그인 사용자")
    void joinRoom_Unauthorized() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Group group = groupRepository.findById(createdGroup.getId()).orElseThrow();
        String inviteCode = group.getRoom().getInviteCode();

        // when & then
        assertThatThrownBy(() -> groupService.joinRoom(inviteCode, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("그룹 참여 실패 - 이미 멤버인 경우")
    void joinRoom_AlreadyMember() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Group group = groupRepository.findById(createdGroup.getId()).orElseThrow();
        String inviteCode = group.getRoom().getInviteCode();

        // when & then
        assertThatThrownBy(() -> groupService.joinRoom(inviteCode, testUser1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_MEMBER);
    }

    @Test
    @DisplayName("그룹 탈퇴 성공")
    void leaveGroup_Success() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Long groupId = createdGroup.getId();
        Group group = groupRepository.findById(groupId).orElseThrow();
        
        // 다른 멤버 추가
        Member member2 = Member.builder()
                .room(group.getRoom())
                .user(testUser2)
                .build();
        memberRepository.save(member2);

        // when
        groupService.leaveGroup(groupId, testUser1);

        // then
        // 첫 번째 멤버는 삭제되었지만 그룹은 유지되어야 함 (다른 멤버가 있으므로)
        assertThat(groupRepository.findById(groupId)).isPresent();
        List<Member> remainingMembers = memberRepository.findByRoomId(group.getRoom().getId());
        assertThat(remainingMembers).hasSize(1);
        assertThat(remainingMembers.get(0).getUser().getId()).isEqualTo(testUser2.getId());
    }

    @Test
    @DisplayName("그룹 탈퇴 성공 - 마지막 멤버 탈퇴 시 Group과 Room 자동 삭제")
    void leaveGroup_LastMember_GroupAndRoomDeleted() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);
        
        Long groupId = createdGroup.getId();

        // when
        groupService.leaveGroup(groupId, testUser1);

        // then
        // Group과 Room이 모두 삭제되어야 함
        assertThat(groupRepository.findById(groupId)).isEmpty();
        
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null) {
            assertThat(roomRepository.findById(group.getRoom().getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 그룹이 존재하지 않음")
    void leaveGroup_GroupNotFound() {
        // when & then
        assertThatThrownBy(() -> groupService.leaveGroup(999L, testUser1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 그룹 멤버가 아님")
    void leaveGroup_NotGroupMember() {
        // given
        GroupResponse createdGroup = groupService.createGroup(createGroupRequest("테스트 그룹", null), testUser1);

        // when & then
        assertThatThrownBy(() -> groupService.leaveGroup(createdGroup.getId(), testUser2))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_GROUP_MEMBER);
    }
}
