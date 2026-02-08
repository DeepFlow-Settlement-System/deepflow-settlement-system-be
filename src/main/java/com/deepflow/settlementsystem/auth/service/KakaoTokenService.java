package com.deepflow.settlementsystem.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KakaoTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Long tokenExpire = 21600000L;
    private final String ACCESS_TOKEN_PREFIX = "KAKAO_ACCESS_TOKEN:";

    public void saveKakaoToken(Long userId, String kakaoToken) {
        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_PREFIX + userId,
                kakaoToken,
                tokenExpire,
                TimeUnit.MILLISECONDS
        );
    }

    public String getKakaoAccessToken(Long userId) {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
    }

    public void deleteKakaoAccessToken(Long userId) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + userId);
    }

    public boolean hasKey(Long userId) {
        return redisTemplate.hasKey(ACCESS_TOKEN_PREFIX + userId);
    }
}