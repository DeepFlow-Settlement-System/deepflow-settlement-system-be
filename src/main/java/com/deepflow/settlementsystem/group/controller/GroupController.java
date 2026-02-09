package com.deepflow.settlementsystem.group.controller;

import com.deepflow.settlementsystem.group.dto.request.*;
import com.deepflow.settlementsystem.group.dto.response.*;
import com.deepflow.settlementsystem.group.service.GroupService;
import com.deepflow.settlementsystem.user.entity.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

@Tag(name = "Group", description = "그룹 관련 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Parameter(description = "그룹 생성 요청 정보", required = true)
            @Valid @RequestBody GroupCreateRequest request,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse response = groupService.createGroup(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사용자 그룹 조회", description = "사용자가 참여한 그룹을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupResponse> responses = groupService.getMyGroups(user);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "특정 그룹 상세 조회", description = "특정 그룹의 상세 정보를 조회합니다. (그룹 맴버 조회)")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        GroupDetailResponse response = groupService.getGroupDetail(groupId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 초대 코드 조회", description = "특정 그룹의 초대 코드와 초대 링크를 조회합니다.")
    @GetMapping("/{groupId}/invite-code")
    public ResponseEntity<InviteCodeResponse> getInviteCode(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        InviteCodeResponse response = groupService.getInviteCode(groupId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 이미지 업로드", description = "그룹 이미지를 업로드합니다.")
    @PostMapping("/{groupId}/image")
    public ResponseEntity<String> uploadGroupImage(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "이미지 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        String imageUrl = groupService.uploadGroupImage(groupId, file, user);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "그룹 이미지 조회", description = "그룹 이미지를 조회합니다.")
    @GetMapping("/{groupId}/image")
    public ResponseEntity<byte[]> getGroupImage(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId) {
        GroupService.ImageData imageData = groupService.getGroupImage(groupId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imageData.getContentType()));
        headers.setContentLength(imageData.getData().length);
        headers.setCacheControl("public, max-age=3600");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(imageData.getData());
    }

    @Operation(summary = "그룹 이미지 삭제", description = "그룹 이미지를 삭제합니다.")
    @DeleteMapping("/{groupId}/image")
    public ResponseEntity<Void> deleteGroupImage(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        groupService.deleteGroupImage(groupId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹 탈퇴", description = "그룹을 탈퇴합니다.")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @Parameter(description = "그룹 ID", required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 로그인한 사용자", required = true, hidden = true)
            @AuthenticationPrincipal User user) {
        groupService.leaveGroup(groupId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "초대 코드로 그룹 정보 조회", description = "초대 코드로 그룹의 정보를 조회합니다. (인증 불필요)")
    @GetMapping("/join")
    public ResponseEntity<GroupJoinInfoResponse> getJoinInfo(
            @Parameter(description = "초대 코드", required = true, example = "abc123def456")
            @RequestParam String code) {
        GroupJoinInfoResponse response = groupService.getJoinInfo(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "초대 코드로 그룹 참여", description = "초대 코드로 그룹에 참여합니다. (인증 선택적)")
    @PostMapping("/join")
    public ResponseEntity<RoomJoinResponse> joinRoom(
            @Parameter(description = "초대 코드", required = true, example = "abc123def456")
            @RequestParam String code) {
        User user = getCurrentUser();
        RoomJoinResponse response = groupService.joinRoom(code, user);
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자 조회 (로그인 상태가 아닐 시 null 반환)
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser")
                && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}