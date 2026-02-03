package com.deepflow.settlementsystem.kakao.service;

import com.deepflow.settlementsystem.kakao.dto.request.KakaoMessageRequest;
import com.deepflow.settlementsystem.kakao.dto.response.KakaoFriendResponse;
import com.deepflow.settlementsystem.kakao.entity.KakaoToken;
import com.deepflow.settlementsystem.kakao.repository.KakaoTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final KakaoTokenRepository kakaoTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${kakao.api-base-url}")
    private String kakaoApiBaseUrl;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    public List<KakaoFriendResponse> getFriends(Long userId) {
        KakaoToken kakaoToken = kakaoTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("카카오 토큰을 찾을 수 없습니다."));

        if (kakaoToken.isExpired()) {
            throw new IllegalArgumentException("카카오 토큰이 만료되었습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoToken.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = kakaoApiBaseUrl + "/v1/api/talk/friends";
        ResponseEntity<KakaoFriendResponse.FriendsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoFriendResponse.FriendsResponse.class
        );

        return response.getBody().getElements();
    }

    public void sendMessage(Long userId, String[] receiverUuids, String message, String linkUrl) {
        KakaoToken kakaoToken = kakaoTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("카카오 토큰을 찾을 수 없습니다."));

        if (kakaoToken.isExpired()) {
            throw new IllegalArgumentException("카카오 토큰이 만료되었습니다.");
        }

        KakaoMessageRequest.TemplateObject.Link link = KakaoMessageRequest.TemplateObject.Link.builder()
                .webUrl(linkUrl)
                .mobileWebUrl(linkUrl)
                .build();

        KakaoMessageRequest.TemplateObject templateObject = KakaoMessageRequest.TemplateObject.builder()
                .objectType("text")
                .text(message)
                .link(link)
                .build();

        KakaoMessageRequest messageRequest = KakaoMessageRequest.builder()
                .receiverUuids(receiverUuids)
                .templateObject(templateObject)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + kakaoToken.getAccessToken());
        HttpEntity<KakaoMessageRequest> entity = new HttpEntity<>(messageRequest, headers);

        String url = kakaoApiBaseUrl + "/v1/api/talk/messages/default";
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}
