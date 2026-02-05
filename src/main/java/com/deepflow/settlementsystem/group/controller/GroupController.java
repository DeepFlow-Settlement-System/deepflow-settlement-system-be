package com.deepflow.settlementsystem.group.controller;

import com.deepflow.settlementsystem.group.dto.request.*;
import com.deepflow.settlementsystem.group.dto.response.*;
import com.deepflow.settlementsystem.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

@Tag(name = "Group", description = "그룹 관련 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        GroupResponse response = groupService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사용자 그룹 조회", description = "사용자가 참여한 그룹을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @AuthenticationPrincipal Long userId) {
        List<GroupResponse> responses = groupService.getMyGroups(userId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "특정 그룹 상세 조회", description = "특정 그룹의 상세 정보를 조회합니다. (그룹 맴버 조회)")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {
        GroupDetailResponse response = groupService.getGroupDetail(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 초대 코드 조회", description = "특정 그룹의 초대 코드와 초대 링크를 조회합니다.")
    @GetMapping("/{groupId}/invite-code")
    public ResponseEntity<InviteCodeResponse> getInviteCode(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {
        InviteCodeResponse response = groupService.getInviteCode(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 탈퇴", description = "그룹을 탈퇴합니다.")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "초대 코드로 그룹 정보 조회", description = "초대 코드로 그룹의 정보를 조회합니다. (인증 불필요)")
    @GetMapping("/join")
    public ResponseEntity<GroupJoinInfoResponse> getJoinInfo(
            @RequestParam String code) {
        GroupJoinInfoResponse response = groupService.getJoinInfo(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "초대 코드로 그룹 참여", description = "초대 코드로 그룹에 참여합니다. (인증 선택적)")
    @PostMapping("/join")
    public ResponseEntity<RoomJoinResponse> joinRoom(
            @RequestParam String code) {
        Long userId = getCurrentUserId();
        RoomJoinResponse response = groupService.joinRoom(code, userId);
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자 ID 조회 (로그인 상태가 아닐 시 null 반환)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}