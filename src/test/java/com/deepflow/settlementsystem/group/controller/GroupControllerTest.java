package com.deepflow.settlementsystem.group.controller;

import com.deepflow.settlementsystem.group.dto.request.GroupCreateRequest;
import com.deepflow.settlementsystem.group.dto.response.GroupDetailResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupJoinInfoResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupResponse;
import com.deepflow.settlementsystem.group.dto.response.InviteCodeResponse;
import com.deepflow.settlementsystem.group.dto.response.RoomJoinResponse;
import com.deepflow.settlementsystem.group.service.GroupService;
import com.deepflow.settlementsystem.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/*
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GroupController 테스트")
class GroupControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public GroupService groupService() {
            return mock(GroupService.class);
        }
    }

    private static final Long TEST_GROUP_ID = 1L;
    
    private User createTestUser() {
        return User.builder()
                .id(1L)
                .kakaoId(1L)
                .username("testuser")
                .nickname("테스트유저")
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("그룹 생성 성공 - 201 Created")
    @WithMockUser(username = "1")
    void createGroup_Success() throws Exception {
        // given
        GroupCreateRequest request = createGroupRequest("테스트 그룹", "테스트 설명");
        GroupResponse response = GroupResponse.builder()
                .id(TEST_GROUP_ID)
                .name("테스트 그룹")
                .description("테스트 설명")
                .inviteCode("testcode123")
                .inviteLink("http://localhost:8080/api/groups/join?code=testcode123")
                .build();

        given(groupService.createGroup(any(GroupCreateRequest.class), any(User.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_GROUP_ID))
                .andExpect(jsonPath("$.name").value("테스트 그룹"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.inviteCode").value("testcode123"));
    }

    @Test
    @DisplayName("그룹 생성 실패 - Validation 실패 (name이 null)")
    @WithMockUser(username = "1")
    void createGroup_ValidationFailed() throws Exception {
        // given
        GroupCreateRequest request = createGroupRequest(null, "설명");

        // when & then
        mockMvc.perform(post("/api/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 그룹 목록 조회 성공 - 200 OK")
    @WithMockUser(username = "1")
    void getMyGroups_Success() throws Exception {
        // given
        List<GroupResponse> responses = Arrays.asList(
                GroupResponse.builder()
                        .id(1L)
                        .name("그룹 1")
                        .build(),
                GroupResponse.builder()
                        .id(2L)
                        .name("그룹 2")
                        .build()
        );

        given(groupService.getMyGroups(any(User.class))).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("그룹 1"))
                .andExpect(jsonPath("$[1].name").value("그룹 2"));
    }

    @Test
    @DisplayName("그룹 상세 조회 성공 - 200 OK")
    @WithMockUser(username = "1")
    void getGroupDetail_Success() throws Exception {
        // given
        GroupDetailResponse response = GroupDetailResponse.builder()
                .id(TEST_GROUP_ID)
                .name("테스트 그룹")
                .description("테스트 설명")
                .inviteCode("testcode123")
                .build();

        given(groupService.getGroupDetail(eq(TEST_GROUP_ID), any(User.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/groups/{groupId}", TEST_GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_GROUP_ID))
                .andExpect(jsonPath("$.name").value("테스트 그룹"))
                .andExpect(jsonPath("$.inviteCode").value("testcode123"));
    }

    @Test
    @DisplayName("초대 코드 조회 성공 - 200 OK")
    @WithMockUser(username = "1")
    void getInviteCode_Success() throws Exception {
        // given
        InviteCodeResponse response = InviteCodeResponse.builder()
                .groupId(TEST_GROUP_ID)
                .groupName("테스트 그룹")
                .inviteCode("testcode123")
                .inviteLink("http://localhost:8080/api/groups/join?code=testcode123")
                .build();

        given(groupService.getInviteCode(eq(TEST_GROUP_ID), any(User.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/groups/{groupId}/invite-code", TEST_GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(TEST_GROUP_ID))
                .andExpect(jsonPath("$.groupName").value("테스트 그룹"))
                .andExpect(jsonPath("$.inviteCode").value("testcode123"));
    }

    @Test
    @DisplayName("그룹 탈퇴 성공 - 204 No Content")
    @WithMockUser(username = "1")
    void leaveGroup_Success() throws Exception {
        // given
        // groupService.leaveGroup은 void 반환

        // when & then
        mockMvc.perform(post("/api/groups/{groupId}/leave", TEST_GROUP_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("초대 코드로 그룹 정보 조회 성공 - 200 OK (인증 불필요)")
    void getJoinInfo_Success() throws Exception {
        // given
        String inviteCode = "testcode123";
        GroupJoinInfoResponse response = GroupJoinInfoResponse.builder()
                .groupId(TEST_GROUP_ID)
                .groupName("테스트 그룹")
                .groupDescription("테스트 설명")
                .inviteCode(inviteCode)
                .build();

        given(groupService.getJoinInfo(inviteCode)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/groups/join")
                        .param("code", inviteCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(TEST_GROUP_ID))
                .andExpect(jsonPath("$.groupName").value("테스트 그룹"))
                .andExpect(jsonPath("$.inviteCode").value(inviteCode));
    }

    @Test
    @DisplayName("초대 코드로 그룹 참여 성공 - 200 OK")
    @WithMockUser(username = "1")
    void joinRoom_Success() throws Exception {
        // given
        String inviteCode = "testcode123";
        RoomJoinResponse response = RoomJoinResponse.builder()
                .groupId(TEST_GROUP_ID)
                .groupName("테스트 그룹")
                .message("그룹에 성공적으로 참여했습니다.")
                .build();

        given(groupService.joinRoom(eq(inviteCode), any(User.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/groups/join")
                        .with(csrf())
                        .param("code", inviteCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(TEST_GROUP_ID))
                .andExpect(jsonPath("$.groupName").value("테스트 그룹"))
                .andExpect(jsonPath("$.message").value("그룹에 성공적으로 참여했습니다."));
    }

    @Test
    @DisplayName("초대 코드로 그룹 참여 실패 - 401 Unauthorized (비로그인)")
    void joinRoom_Unauthorized() throws Exception {
        // given
        String inviteCode = "testcode123";
        given(groupService.joinRoom(eq(inviteCode), eq(null)))
                .willThrow(new com.deepflow.settlementsystem.common.exception.CustomException(
                        com.deepflow.settlementsystem.common.code.ErrorCode.UNAUTHORIZED));

        // when & then
        mockMvc.perform(post("/api/groups/join")
                        .with(csrf())
                        .param("code", inviteCode))
                .andExpect(status().isUnauthorized());
    }

    private GroupCreateRequest createGroupRequest(String name, String description) {
        GroupCreateRequest request = new GroupCreateRequest();
        try {
            java.lang.reflect.Field nameField = GroupCreateRequest.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(request, name);

            if (description != null) {
                java.lang.reflect.Field descField = GroupCreateRequest.class.getDeclaredField("description");
                descField.setAccessible(true);
                descField.set(request, description);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GroupCreateRequest", e);
        }
        return request;
    }
}
*/