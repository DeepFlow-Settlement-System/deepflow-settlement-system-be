package com.deepflow.settlementsystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@AllArgsConstructor
public class KakaoUserInfo {
    private Long id;
    private ConcurrentHashMap<String, String> properties;
}
