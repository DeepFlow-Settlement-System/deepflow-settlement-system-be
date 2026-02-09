package com.deepflow.settlementsystem.settlement.service;

import com.deepflow.settlementsystem.auth.config.KakaoApiUrl;
import com.deepflow.settlementsystem.auth.service.KakaoTokenService;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.expense.entity.Expense;
import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import com.deepflow.settlementsystem.expense.entity.ExpenseItem;
import com.deepflow.settlementsystem.expense.entity.SettlementStatus;
import com.deepflow.settlementsystem.expense.entity.SettlementType;
import com.deepflow.settlementsystem.expense.repository.ExpenseItemAllocationRepository;
import com.deepflow.settlementsystem.settlement.dto.SettlementItem;
import com.deepflow.settlementsystem.settlement.dto.request.KakaoMessageRequest;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoFriendsResponse;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoSendMessageResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementSummaryResponse;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {
    private final RestClient restClient;
    private final KakaoTokenService kakaoTokenService;
    private final ObjectMapper objectMapper;
    private final ExpenseItemAllocationRepository expenseAllocationRepository;
    private final UserRepository userRepository;
    
    /**
     * 정산 요청 메시지 전송
     * 돈을 받는 사람(receiver)이 돈을 보낼 사람(sender)에게 카카오톡 메시지를 전송합니다.
     * 상태를 선택하여 요청하거나 통합적으로 한번에 요청할 수 있습니다.
     */
    @Transactional
    public void sendSettlementMessage(Long targetUserId, List<SettlementStatus> statuses, Long receiverUserId) {
        validateNotNull(targetUserId, "targetUserId");
        validateNotNull(receiverUserId, "receiverUserId");
        
        // 상태 필터 설정: null이면 UNSETTLED, REQUESTED 모두 포함
        List<SettlementStatus> targetStatuses = (statuses == null || statuses.isEmpty()) 
            ? List.of(SettlementStatus.UNSETTLED, SettlementStatus.REQUESTED)
            : statuses;
        
        // 두 사용자 간의 정산 건 조회 (상태 필터 적용)
        List<ExpenseAllocation> allocations = expenseAllocationRepository
                .findBySenderIdAndReceiverIdAndStatusIn(targetUserId, receiverUserId, targetStatuses);
        
        if (allocations.isEmpty()) {
            throw new CustomException(ErrorCode.NO_SETTLEMENT);
        }
        
        // 첫 번째 allocation에서 receiver 정보 가져오기
        ExpenseAllocation firstAllocation = allocations.get(0);
        validateAllocationNotNull(firstAllocation);
        
        User receiver = firstAllocation.getReceiver();
        User sender = firstAllocation.getSender();
        
        // 권한 확인
        if (!receiver.getId().equals(receiverUserId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        // COMPLETED 상태는 제외
        allocations = allocations.stream()
                .filter(allocation -> allocation.getStatus() != SettlementStatus.COMPLETED)
                .collect(Collectors.toList());
        
        if (allocations.isEmpty()) {
            throw new CustomException(ErrorCode.NO_SETTLEMENT);
        }
        
        // 총 금액 계산
        Long totalAmount = allocations.stream()
                .mapToLong(allocation -> allocation.getShareAmount().longValue())
                .sum();
        
        // 필수 정보 확인
        if (receiver.getKakaoPaySuffix() == null || receiver.getKakaoPaySuffix().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        String accessToken = kakaoTokenService.getKakaoAccessToken(receiverUserId);
        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("카카오 Access Token이 없습니다. receiverUserId: {}", receiverUserId);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // sender의 UUID 찾기
        String senderUuid = findUserUuidByUserId(accessToken, sender.getId());
        
        // 송금 링크 생성
        String paymentLink = generatePaymentLink(receiver.getKakaoPaySuffix(), totalAmount);
        String groupName = firstAllocation.getGroup().getName();
        
        // 모든 정산 건의 아이템 수집
        List<SettlementItem> items = getAllocationItems(allocations);
        
        // 메시지 생성
        KakaoMessageRequest message = createSettlementMessage(
                paymentLink,
                groupName,
                items,
                totalAmount
        );
        
        // 카카오 메시지 전송
        sendKakaoMessage(accessToken, senderUuid, message);
        
        // 모든 allocation의 상태를 REQUESTED로 변경
        allocations.forEach(allocation -> {
            allocation.setStatus(SettlementStatus.REQUESTED);
            expenseAllocationRepository.save(allocation);
        });
    }
    
    /**
     * 카카오 친구 목록에서 특정 사용자의 UUID를 찾습니다.
     * @param accessToken 카카오 Access Token
     * @param targetUserId 찾을 사용자의 ID
     * @return 사용자의 UUID
     */
    private String findUserUuidByUserId(String accessToken, Long targetUserId) {
        String currentAfterUrl = null;
        int maxPages = 1000;
        int pageCount = 0;
        
        do {
            KakaoFriendsResponse friendsResponse = getKakaoFriends(accessToken, currentAfterUrl);
            
            String userUuid = findUserUuidInFriends(friendsResponse, targetUserId);
            if (userUuid != null) {
                return userUuid;
            }
            
            currentAfterUrl = friendsResponse.getAfterUrl();
            pageCount++;
            
        } while (currentAfterUrl != null && pageCount < maxPages);
        
        log.warn("카카오 친구 목록에서 사용자를 찾지 못했습니다. targetUserId: {}", targetUserId);
        throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    
    private KakaoFriendsResponse getKakaoFriends(String accessToken, String afterUrl) {
        String url = afterUrl != null 
            ? afterUrl 
            : KakaoApiUrl.FRIENDS.getUrl();
        
        KakaoFriendsResponse response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(url).build().toUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                    log.error("카카오 친구 목록 API 호출 실패: {}", httpResponse.getStatusCode());
                    throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                })
                .body(KakaoFriendsResponse.class);
        
        return Objects.requireNonNull(response);
    }
    
    /**
     * 카카오 친구 목록 응답에서 특정 사용자 ID의 UUID를 찾습니다.
     * @param response 카카오 친구 목록 응답
     * @param targetUserId 찾을 사용자의 ID
     * @return 사용자의 UUID, 없으면 null
     */
    private String findUserUuidInFriends(KakaoFriendsResponse response, Long targetUserId) {
        if (response.getElements() == null || response.getElements().isEmpty()) {
            return null;
        }
        
        return response.getElements().stream()
                .filter(friend -> friend.getId() != null && friend.getId().equals(targetUserId))
                .map(KakaoFriendsResponse.Friend::getUuid)
                .filter(uuid -> uuid != null && !uuid.isEmpty())
                .findFirst()
                .orElse(null);
    }
    
    private String generatePaymentLink(String kakaoPaySuffix, Long amount) {
        validateNotBlank(kakaoPaySuffix, "kakaoPaySuffix");
        validatePositive(amount, "amount");
        
        long multipliedAmount = amount * 8;
        String hexAmount = Long.toHexString(multipliedAmount).toUpperCase();
        
        // 랜덤 숫자 4자리 생성
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
        String randomSuffix = String.format("%04d", randomNumber);
        
        // 링크
        return "https://qr.kakaopay.com/" + kakaoPaySuffix + hexAmount + randomSuffix;
    }
    
    private KakaoMessageRequest createSettlementMessage(
            String paymentLink,
            String groupName,
            List<SettlementItem> items,
            Long totalAmount) {
        
        // items 생성
        List<KakaoMessageRequest.Item> messageItems = new ArrayList<>();

        // 그룹 이름
        messageItems.add(KakaoMessageRequest.Item.builder()
                .item("그룹")
                .itemOp(groupName)
                .build());
        
        // 지출 내역
        for (SettlementItem item : items) {
            messageItems.add(KakaoMessageRequest.Item.builder()
                    .item(item.getDescription())
                    .itemOp(formatAmount(item.getAmount()))
                    .build());
        }
        
        // 링크 생성
        KakaoMessageRequest.Link link = KakaoMessageRequest.Link.builder()
                .webUrl(paymentLink)
                .mobileWebUrl(paymentLink)
                .build();
        
        // 메시지 생성
        return KakaoMessageRequest.builder()
                .objectType("feed")
                .content(KakaoMessageRequest.Content.builder()
                        .title("💸 송금 부탁드립니다.")
                        .imageUrl("https://plus.unsplash.com/premium_photo-1679830513869-cd3648acb1db?q=80&w=927&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                        .imageWidth(640)
                        .imageHeight(640)
                        .link(link)
                        .build())
                .itemContent(KakaoMessageRequest.ItemContent.builder()
                        .profileText("정산 요청")
                        .items(messageItems)
                        .sum("총 송금 금액")
                        .sumOp(formatAmount(totalAmount))
                        .build())
                .buttons(List.of(KakaoMessageRequest.Button.builder()
                        .title("카카오페이로 송금하기")
                        .link(link)
                        .build()))
                .build();
    }
    
    private String formatAmount(Long amount) {
        return String.format("%,d원", amount);
    }
    
    private void sendKakaoMessage(String accessToken, String receiverUuid, KakaoMessageRequest message) {
        validateNotBlank(accessToken, "accessToken");
        validateNotBlank(receiverUuid, "receiverUuid");
        
        try {
            String receiverUuidsJson = objectMapper.writeValueAsString(List.of(receiverUuid));
            String templateObjectJson = objectMapper.writeValueAsString(message);
            String requestBody = "receiver_uuids=" + URLEncoder.encode(receiverUuidsJson, StandardCharsets.UTF_8)
                    + "&template_object=" + URLEncoder.encode(templateObjectJson, StandardCharsets.UTF_8);
            
            KakaoSendMessageResponse response = restClient.post()
                    .uri(KakaoApiUrl.SEND_MESSAGE.getUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                        log.error("카카오 메시지 전송 API 호출 실패: {}", httpResponse.getStatusCode());
                        throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                    })
                    .body(KakaoSendMessageResponse.class);
            
            // successful_receiver_uuids에 receiverUuid가 포함되어 있는지 확인
            if (response.getSuccessfulReceiverUuids() == null || 
                response.getSuccessfulReceiverUuids().isEmpty() ||
                !response.getSuccessfulReceiverUuids().contains(receiverUuid)) {
                log.error("카카오 메시지 전송 실패 - receiverUuid가 성공 목록에 없습니다. receiverUuid: {}", receiverUuid);
                throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
            }
            
            log.info("카카오 메시지 전송 성공. receiverUuid: {}", receiverUuid);
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON 직렬화 중 오류 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("카카오 메시지 전송 중 오류 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
        }
    }
    
    // 정산 상태 조회
    public SettlementResponse getSettlementStatus(Long allocationId, Long userId) {
        ExpenseAllocation allocation = expenseAllocationRepository.findByIdWithRelations(allocationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_SETTLEMENT));
        
        validateAllocationNotNull(allocation);
        
        if (!allocation.getSender().getId().equals(userId) && !allocation.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        return toSettlementResponse(allocation);
    }
    
    /**
     * 정산 완료 처리
     * 돈을 받는 사람(receiver)이 송금 수령 확인 후 완료 처리합니다.
     */
    @Transactional
    public void completeSettlement(Long allocationId, Long userId) {
        ExpenseAllocation allocation = expenseAllocationRepository.findByIdWithRelations(allocationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_SETTLEMENT));
        
        validateAllocationNotNull(allocation);
        
        // 돈을 받는 사람(receiver)만 완료 처리 가능
        if (!allocation.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        if (allocation.getStatus() != SettlementStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // 상태를 COMPLETED로 변경
        allocation.setStatus(SettlementStatus.COMPLETED);
        expenseAllocationRepository.save(allocation);
    }
    
    // 정산 목록 조회
    public SettlementListResponse getSettlementList(Long userId) {
        List<ExpenseAllocation> allocations = expenseAllocationRepository.findByUserId(userId);
        
        List<SettlementResponse> settlements = allocations.stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());
        
        return SettlementListResponse.builder()
                .settlements(settlements)
                .totalCount((long) settlements.size())
                .build();
    }
    
    // ExpenseAllocation에서 SettlementItem 리스트 생성
    private List<SettlementItem> getSettlementItems(ExpenseAllocation allocation) {
        List<SettlementItem> items = new ArrayList<>();
        Expense expense = allocation.getExpense();
        
        if (expense == null) {
            return items;
        }
        
        // 1/N인 경우
        if (expense.getSettlementType() == SettlementType.N_BBANG) {
            items.add(new SettlementItem(expense.getTitle(), expense.getTotalAmount().longValue()));
        } 
        // 품목별인 경우
        else if (expense.getSettlementType() == SettlementType.ITEMIZED && allocation.getItem() != null) {
            ExpenseItem item = allocation.getItem();
            items.add(new SettlementItem(item.getItemName(), item.getLineAmount().longValue()));
        }
        
        return items;
    }
    
    // 여러 allocation에서 아이템 수집
    private List<SettlementItem> getAllocationItems(List<ExpenseAllocation> allocations) {
        List<SettlementItem> items = new ArrayList<>();
        
        for (ExpenseAllocation allocation : allocations) {
            Expense expense = allocation.getExpense();
            if (expense == null) continue;
            
            if (expense.getSettlementType() == SettlementType.N_BBANG) {
                items.add(new SettlementItem(expense.getTitle(), expense.getTotalAmount().longValue()));
            } else if (expense.getSettlementType() == SettlementType.ITEMIZED && allocation.getItem() != null) {
                ExpenseItem item = allocation.getItem();
                items.add(new SettlementItem(item.getItemName(), item.getLineAmount().longValue()));
            }
        }
        
        return items;
    }
    
    /**
     * 로그인한 사용자와 특정 사용자 간의 정산 건을 상태별로 묶어서 금액 조회
     */
    public SettlementSummaryResponse getSettlementSummary(Long userId, Long targetUserId) {
        validateNotNull(userId, "userId");
        validateNotNull(targetUserId, "targetUserId");
        
        // 두 사용자 간의 모든 정산 건 조회 (양방향)
        List<ExpenseAllocation> allocations = expenseAllocationRepository
                .findBySenderIdAndReceiverId(targetUserId, userId);
        
        List<ExpenseAllocation> reverseAllocations = expenseAllocationRepository
                .findBySenderIdAndReceiverId(userId, targetUserId);
        
        // 두 방향 모두 합치기
        List<ExpenseAllocation> allAllocations = new ArrayList<>();
        allAllocations.addAll(allocations);
        allAllocations.addAll(reverseAllocations);
        
        // 상대방 사용자 정보 조회
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (allAllocations.isEmpty()) {
            return SettlementSummaryResponse.builder()
                    .targetUserId(targetUserId)
                    .targetUserNickname(targetUser.getNickname())
                    .totalUnsettledAmount(0L)
                    .totalRequestedAmount(0L)
                    .totalCompletedAmount(0L)
                    .totalAmount(0L)
                    .build();
        }
        
        // 상태별 금액 계산
        Long totalUnsettledAmount = allAllocations.stream()
                .filter(a -> a.getStatus() == SettlementStatus.UNSETTLED)
                .mapToLong(a -> a.getShareAmount().longValue())
                .sum();
        
        Long totalRequestedAmount = allAllocations.stream()
                .filter(a -> a.getStatus() == SettlementStatus.REQUESTED)
                .mapToLong(a -> a.getShareAmount().longValue())
                .sum();
        
        Long totalCompletedAmount = allAllocations.stream()
                .filter(a -> a.getStatus() == SettlementStatus.COMPLETED)
                .mapToLong(a -> a.getShareAmount().longValue())
                .sum();
        
        Long totalAmount = allAllocations.stream()
                .mapToLong(a -> a.getShareAmount().longValue())
                .sum();
        
        return SettlementSummaryResponse.builder()
                .targetUserId(targetUserId)
                .targetUserNickname(targetUser.getNickname())
                .totalUnsettledAmount(totalUnsettledAmount)
                .totalRequestedAmount(totalRequestedAmount)
                .totalCompletedAmount(totalCompletedAmount)
                .totalAmount(totalAmount)
                .build();
    }
    
    // ExpenseAllocation을 SettlementResponse로 변환
    private SettlementResponse toSettlementResponse(ExpenseAllocation allocation) {
        validateAllocationNotNull(allocation);
        
        return SettlementResponse.builder()
                .allocationId(allocation.getAllocationId())
                .groupId(allocation.getGroup().getId())
                .groupName(allocation.getGroup().getName())
                .expenseId(allocation.getExpense() != null ? allocation.getExpense().getExpenseId() : null)
                .expenseTitle(allocation.getExpense() != null ? allocation.getExpense().getTitle() : null)
                .senderId(allocation.getSender().getId())
                .senderNickname(allocation.getSender().getNickname())
                .receiverId(allocation.getReceiver().getId())
                .receiverNickname(allocation.getReceiver().getNickname())
                .amount(allocation.getShareAmount().longValue())
                .status(allocation.getStatus())
                .createdAt(allocation.getCreatedAt())
                .build();
    }
    
    /**
     * ExpenseAllocation의 필수 필드 null 검증
     * @param allocation 검증할 ExpenseAllocation
     */
    private void validateAllocationNotNull(ExpenseAllocation allocation) {
        if (allocation.getGroup() == null) {
            throw new CustomException(ErrorCode.NO_SETTLEMENT);
        }
        if (allocation.getSender() == null) {
            throw new CustomException(ErrorCode.NO_SETTLEMENT);
        }
        if (allocation.getReceiver() == null) {
            throw new CustomException(ErrorCode.NO_SETTLEMENT);
        }
    }
    
    /**
     * 값이 null인지 검증
     * @param value 검증할 값
     * @param fieldName 필드명 (에러 식별용)
     */
    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
    
    /**
     * 문자열이 null이거나 비어있는지 검증
     * @param value 검증할 문자열
     * @param fieldName 필드명 (에러 식별용)
     */
    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
    
    /**
     * 양수인지 검증
     * @param value 검증할 값
     * @param fieldName 필드명 (에러 식별용)
     */
    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
    
}
