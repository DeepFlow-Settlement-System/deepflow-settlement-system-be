package com.deepflow.settlementsystem.settlement.service;

import com.deepflow.settlementsystem.auth.config.KakaoApiUrl;
import com.deepflow.settlementsystem.auth.service.KakaoTokenService;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.settlement.dto.SettlementItem;
import com.deepflow.settlementsystem.settlement.dto.request.KakaoMessageRequest;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoFriendsResponse;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {
    
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final KakaoTokenService kakaoTokenService;
    
    public String sendSettlementMessage(Long senderUserId, Long receiverUserId, Long amount) {
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // KakaoTokenService로 access token 조회
        String accessToken = kakaoTokenService.getKakaoAccessToken(senderUserId);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 로그인한 유저의 kakaoPaySuffix 확인
        if (sender.getKakaoPaySuffix() == null || sender.getKakaoPaySuffix().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // 첫 페이지 조회
        String currentAfterUrl = null;
        int maxPages = 1000; // 최대 페이지 수 제한
        int pageCount = 0;
        
        do {
            KakaoFriendsResponse friendsResponse = getKakaoFriends(accessToken, currentAfterUrl);
            
            // 친구 목록에서 receiverUserId 찾기
            String receiverUuid = findReceiverUuid(friendsResponse, receiverUserId);
            if (receiverUuid != null) {
                // 송금 링크 생성
                String paymentLink = generatePaymentLink(sender.getKakaoPaySuffix(), amount);
                
                // 하드코딩된 값으로 메시지 생성
                String groupName = "그룹명"; // TODO: 차후 다른 도메인에서 가져올 예정
                List<SettlementItem> items = List.of(
                        new SettlementItem("아이템", 12000L),
                        new SettlementItem("아이템", 11111L)
                ); // TODO: 차후 다른 도메인에서 가져올 예정
                
                KakaoMessageRequest message = createSettlementMessage(
                        paymentLink,
                        groupName,
                        items,
                        amount
                );
                
                // TODO: 카카오 메시지 전송 API 호출
                
                return receiverUuid;
            }
            
            // 다음 페이지 URL 업데이트
            currentAfterUrl = friendsResponse.getAfterUrl();
            pageCount++;
            
        } while (currentAfterUrl != null && pageCount < maxPages);
        
        // 최대 페이지 수 초과 시 로그
        if (pageCount >= maxPages) {
            log.warn("친구 목록 검색 중 최대 페이지 수({})에 도달했습니다. senderUserId: {}, receiverUserId: {}", 
                    maxPages, senderUserId, receiverUserId);
        }
        
        // 친구 목록에서 찾지 못함
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
    
    private String findReceiverUuid(KakaoFriendsResponse response, Long receiverUserId) {
        if (response.getElements() == null || response.getElements().isEmpty()) {
            return null;
        }
        
        return response.getElements().stream()
                .filter(friend -> friend.getId() != null && friend.getId().equals(receiverUserId))
                .map(KakaoFriendsResponse.Friend::getUuid)
                .findFirst()
                .orElse(null);
    }
    
    private String generatePaymentLink(String kakaoPaySuffix, Long amount) {
        // 금액에 8을 곱하고 16진수로 변환 (대문자)
        long multipliedAmount = amount * 8;
        String hexAmount = Long.toHexString(multipliedAmount).toUpperCase();
        
        // 랜덤 숫자 4자리 생성 (0000~9999)
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
        String randomSuffix = String.format("%04d", randomNumber);
        
        // 링크 조합
        return "https://qr.kakaopay.com/" + kakaoPaySuffix + hexAmount + randomSuffix;
    }
    
    private KakaoMessageRequest createSettlementMessage(
            String paymentLink,
            String groupName,
            List<SettlementItem> items,
            Long totalAmount) {
        
        // items 생성
        List<KakaoMessageRequest.Item> messageItems = new ArrayList<>();
        messageItems.add(KakaoMessageRequest.Item.builder()
                .item("그룹")
                .itemOp(groupName)
                .build());
        
        // 지출 내역 추가
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
    
}
