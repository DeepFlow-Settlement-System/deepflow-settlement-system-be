package com.deepflow.settlementsystem.auth.service;

import com.deepflow.settlementsystem.auth.config.KakaoApiUrl;
import com.deepflow.settlementsystem.auth.config.KakaoProperties;
import com.deepflow.settlementsystem.auth.dto.*;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.security.JwtTokenProvider;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RestClient restClient;
    private final KakaoProperties kakaoProperties;

    public LoginResponse kakaoLogin(KakaoLoginRequest loginRequest) {
        String accessToken = getKakaoAccessToken(loginRequest);
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);
        User user = userService.getUserOrCreate(kakaoUserInfo);

        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.createToken(user))
                .build();
    }

    public KakaoLoginUrlResponse getKakaoLoginUrl() {
        String resultUrl = UriComponentsBuilder.fromPath(KakaoApiUrl.CODE.getUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoProperties.getClientId())
                .queryParam("redirect_uri", kakaoProperties.getRedirectUrl())
                .encode()
                .toUriString();

        return new KakaoLoginUrlResponse(resultUrl);
    }

    private KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        KakaoUserInfo userInfo = restClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(KakaoApiUrl.ME.getUrl())
                        .build().toUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("카카오 User Info API 호출 실패: {}", response.getStatusCode());
                    throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                })
                .body(KakaoUserInfo.class);

        return userInfo;
    }

    private String getKakaoAccessToken(KakaoLoginRequest loginRequest) {
        KakaoTokenResponse tokenResponse = restClient.post()
                .uri(UriComponentsBuilder
                        .fromUriString(KakaoApiUrl.TOKEN.getUrl())
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", kakaoProperties.getClientId())
                        .queryParam("redirect_uri", kakaoProperties.getRedirectUrl())
                        .queryParam("client_secret", kakaoProperties.getClientSecret())
                        .queryParam("code", loginRequest.getCode())
                        .build().toUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("카카오 Access Token API 호출 실패: {}", response.getStatusCode());
                    throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                })
                .body(KakaoTokenResponse.class);

        return Objects.requireNonNull(tokenResponse).getAccessToken();
    }
}
