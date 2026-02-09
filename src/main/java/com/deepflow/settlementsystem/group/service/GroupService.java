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
import com.deepflow.settlementsystem.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3MB

    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 그룹 생성
     * @param request 그룹 생성 요청 정보
     * @param user 그룹을 생성하는 사용자
     * @return 생성된 그룹 정보
     */
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, User user) {
        // 그룹 생성
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
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
                .user(user)
                .build();
        memberRepository.save(firstMember);

        return toGroupResponse(group, room);
    }

    /**
     * 사용자가 참여한 그룹 목록 조회
     * @param user 조회할 사용자
     * @return 사용자가 참여한 그룹 목록
     */
    public List<GroupResponse> getMyGroups(User user) {
        List<Group> groups = groupRepository.findAllByUserId(user.getId());
        return groups.stream()
                .filter(group -> group.getRoom() != null)
                .map(group -> toGroupResponse(group, group.getRoom()))
                .collect(Collectors.toList());
    }

    /**
     * 그룹 상세 정보 조회
     * @param groupId 그룹 ID
     * @param user 조회하는 사용자
     * @return 그룹 상세 정보 및 멤버 목록
     */
    public GroupDetailResponse getGroupDetail(Long groupId, User user) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), user.getId());
        if (!isMember) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }

        // 멤버 목록 조회
        List<Member> members = memberRepository.findByRoomId(group.getRoom().getId());
        List<MemberResponse> memberResponses = members.stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .userId(member.getUser().getId())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        String imageUrl = group.getImageData() != null 
                ? baseUrl + "/api/groups/" + groupId + "/image" 
                : null;

        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .imageUrl(imageUrl)
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
                .inviteCode(group.getRoom().getInviteCode())
                .inviteLink(generateInviteLink(group.getRoom().getInviteCode()))
                .members(memberResponses)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    /**
     * 그룹 초대 코드 조회
     * @param groupId 그룹 ID
     * @param user 조회하는 사용자
     * @return 그룹 초대 코드 및 초대 링크
     */
    public InviteCodeResponse getInviteCode(Long groupId, User user) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), user.getId());
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

    /**
     * 초대 코드로 그룹 정보 조회
     * @param inviteCode 초대 코드
     * @return 그룹 정보 (인증 불필요)
     */
    public GroupJoinInfoResponse getJoinInfo(String inviteCode) {
        Room room = roomRepository.findByInviteCodeWithGroup(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        if (room.getGroup() == null) {
            throw new CustomException(ErrorCode.GROUP_NOT_FOUND);
        }

        String imageUrl = room.getGroup().getImageData() != null 
                ? baseUrl + "/api/groups/" + room.getGroup().getId() + "/image" 
                : null;

        return GroupJoinInfoResponse.builder()
                .groupId(room.getGroup().getId())
                .groupName(room.getGroup().getName())
                .groupDescription(room.getGroup().getDescription())
                .imageUrl(imageUrl)
                .startDate(room.getGroup().getStartDate())
                .endDate(room.getGroup().getEndDate())
                .inviteCode(inviteCode)
                .build();
    }

    /**
     * 그룹 참여
     * @param inviteCode 초대 코드
     * @param user 참여할 사용자 (null이면 예외 발생)
     * @return 그룹 참여 결과
     */
    @Transactional
    public RoomJoinResponse joinRoom(String inviteCode, User user) {
        Room room = roomRepository.findByInviteCodeWithGroup(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        if (room.getGroup() == null) {
            throw new CustomException(ErrorCode.GROUP_NOT_FOUND);
        }

        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 이미 멤버인지 확인
        if (memberRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) {
            throw new CustomException(ErrorCode.ALREADY_MEMBER);
        }

        // 멤버 추가
        Member member = Member.builder()
                .room(room)
                .user(user)
                .build();
        memberRepository.save(member);

        return RoomJoinResponse.builder()
                .groupId(room.getGroup().getId())
                .groupName(room.getGroup().getName())
                .message("그룹에 성공적으로 참여했습니다.")
                .build();
    }

    /**
     * 그룹 탈퇴
     * @param groupId 그룹 ID
     * @param user 탈퇴할 사용자
     */
    @Transactional
    public void leaveGroup(Long groupId, User user) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        if (group.getRoom() == null) {
            throw new CustomException(ErrorCode.GROUP_NOT_FOUND);
        }

        Member member = memberRepository.findByRoomIdAndUserId(group.getRoom().getId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_GROUP_MEMBER));

        memberRepository.delete(member);

        // 모든 인원이 나갔는지 확인
        List<Member> remainingMembers = memberRepository.findByRoomId(group.getRoom().getId());
        if (remainingMembers.isEmpty()) {
            groupRepository.delete(group);
        }
    }

    /**
     * 그룹 이미지 업로드
     * @param groupId 그룹 ID
     * @param file 업로드할 이미지 파일
     * @param user 업로드하는 사용자
     * @return 업로드된 이미지 URL
     */
    @Transactional
    public String uploadGroupImage(Long groupId, MultipartFile file, User user) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), user.getId());
        if (!isMember) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }

        // 파일 유효성 검증
        validateImageFile(file);

        try {
            // 바이트 배열로 변환
            byte[] imageData = file.getBytes();
            String contentType = file.getContentType();

            // 그룹에 이미지 저장
            group.updateImage(imageData, contentType);
            groupRepository.save(group);

            // 이미지 URL 반환
            String imageUrl = baseUrl + "/api/groups/" + groupId + "/image";
            log.info("그룹 이미지 업로드 완료: groupId={}", groupId);
            return imageUrl;

        } catch (IOException e) {
            log.error("이미지 파일 읽기 중 오류 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 그룹 이미지 삭제
     * @param groupId 그룹 ID
     * @param user 삭제하는 사용자
     */
    @Transactional
    public void deleteGroupImage(Long groupId, User user) {
        Group group = groupRepository.findByIdWithRoom(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = memberRepository.existsByRoomIdAndUserId(group.getRoom().getId(), user.getId());
        if (!isMember) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }

        group.deleteImage();
        groupRepository.save(group);
        log.info("그룹 이미지 삭제 완료: groupId={}", groupId);
    }

    /**
     * 그룹 이미지 조회
     * @param groupId 그룹 ID
     * @return 이미지 데이터와 Content-Type
     */
    public ImageData getGroupImage(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        if (group.getImageData() == null || group.getImageData().length == 0) {
            throw new CustomException(ErrorCode.GROUP_NOT_FOUND);
        }

        return new ImageData(group.getImageData(), group.getImageContentType());
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 이미지 데이터와 Content-Type을 담는 클래스
     */
    public static class ImageData {
        private final byte[] data;
        private final String contentType;

        public ImageData(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType != null ? contentType : "image/jpeg";
        }

        public byte[] getData() {
            return data;
        }

        public String getContentType() {
            return contentType;
        }
    }

    private GroupResponse toGroupResponse(Group group, Room room) {
        String imageUrl = group.getImageData() != null 
                ? baseUrl + "/api/groups/" + group.getId() + "/image" 
                : null;

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .imageUrl(imageUrl)
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
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
