package com.deepflow.settlementsystem.settlement.service;

import com.deepflow.settlementsystem.auth.config.KakaoApiUrl;
import com.deepflow.settlementsystem.auth.service.KakaoTokenService;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.settlement.dto.SettlementItem;
import com.deepflow.settlementsystem.settlement.dto.request.KakaoMessageRequest;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoFriendsResponse;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoSendMessageResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {
    
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final KakaoTokenService kakaoTokenService;
    private final ObjectMapper objectMapper;
    
    public void sendSettlementMessage(Long senderUserId, Long receiverUserId, Long amount) {
        // 입력값 검증
        if (senderUserId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (receiverUserId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (sender.getKakaoPaySuffix() == null || sender.getKakaoPaySuffix().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // Access token 조회
        String accessToken = kakaoTokenService.getKakaoAccessToken(senderUserId);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 받는 사람의 UUID
        String receiverUuid = findReceiverUuidByUserId(accessToken, receiverUserId);
        
        // 송금 링크 생성
        String paymentLink = generatePaymentLink(sender.getKakaoPaySuffix(), amount);
        
        //메시지 생성
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
        
        // 카카오 메시지 전송 API 호출
        sendKakaoMessage(accessToken, receiverUuid, message);
    }
    
    private String findReceiverUuidByUserId(String accessToken, Long receiverUserId) {
        // 친구 목록에서 찾기 (페이지네이션 포함)
        String currentAfterUrl = null;
        int maxPages = 1000;
        int pageCount = 0;
        
        do {
            KakaoFriendsResponse friendsResponse = getKakaoFriends(accessToken, currentAfterUrl);
            
            String receiverUuid = findReceiverUuid(friendsResponse, receiverUserId);
            if (receiverUuid != null) {
                return receiverUuid;
            }
            
            currentAfterUrl = friendsResponse.getAfterUrl();
            pageCount++;
            
        } while (currentAfterUrl != null && pageCount < maxPages);
        
        if (pageCount >= maxPages) {
            log.warn("친구 목록 검색 중 최대 페이지 수({})에 도달했습니다. receiverUserId: {}", 
                    maxPages, receiverUserId);
        }
        
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
                .filter(uuid -> uuid != null && !uuid.isEmpty())
                .findFirst()
                .orElse(null);
    }
    
    private String generatePaymentLink(String kakaoPaySuffix, Long amount) {
        // 입력값 검증
        if (kakaoPaySuffix == null || kakaoPaySuffix.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
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
        // 입력값 검증
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (receiverUuid == null || receiverUuid.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        try {
            // receiver_uuids를 JSON 배열 문자열로 변환: ["uuid"]
            String receiverUuidsJson = objectMapper.writeValueAsString(List.of(receiverUuid));
            
            // template_object를 JSON 문자열로 변환
            String templateObjectJson = objectMapper.writeValueAsString(message);
            
            // form-urlencoded 형식으로 요청 본문 생성
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
    
}
