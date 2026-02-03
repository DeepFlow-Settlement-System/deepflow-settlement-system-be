package com.deepflow.settlementsystem.kakao.repository;

import com.deepflow.settlementsystem.kakao.entity.KakaoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KakaoTokenRepository extends JpaRepository<KakaoToken, Long> {
    Optional<KakaoToken> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
