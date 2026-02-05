package com.deepflow.settlementsystem.user.dto;

import com.deepflow.settlementsystem.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private Long kakaoId;
    private String username;
    private String nickname;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
