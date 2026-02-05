package com.deepflow.settlementsystem.user.service;

import com.deepflow.settlementsystem.auth.dto.KakaoUserInfo;
import com.deepflow.settlementsystem.user.dto.UserDto;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User getUserOrCreate(KakaoUserInfo kakaoUserInfo) {
        return userRepository.findByKakaoId(kakaoUserInfo.getId())
                .orElseGet(() -> userRepository.save(
                                User.builder()
                                        .kakaoId(kakaoUserInfo.getId())
                                        .username(UUID.randomUUID().toString())
                                        .nickname(kakaoUserInfo.getProperties().get("nickname"))
                                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                        .build()
                        )
                );
    }
}
