package com.deepflow.settlementsystem.auth.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KakaoApiUrl {
    CODE("https://kauth.kakao.com/oauth/authorize"),
    TOKEN("https://kauth.kakao.com/oauth/token"),
    ME("https://kapi.kakao.com/v2/user/me"),
    LOGOUT("https://kapi.kakao.com/v1/user/logout"),
    FRIENDS("https://kapi.kakao.com/v1/api/talk/friends"),
    SEND_MESSAGE("https://kapi.kakao.com/v1/api/talk/friends/message/default/send");

    private final String url;
}
