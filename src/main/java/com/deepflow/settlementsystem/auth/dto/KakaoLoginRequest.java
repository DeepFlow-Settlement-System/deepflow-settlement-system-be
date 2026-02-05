package com.deepflow.settlementsystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginRequest {
    private String code;
}
