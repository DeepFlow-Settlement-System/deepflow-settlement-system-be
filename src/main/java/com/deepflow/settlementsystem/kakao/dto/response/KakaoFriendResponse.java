package com.deepflow.settlementsystem.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoFriendResponse {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("favorite")
    private Boolean favorite;

    @JsonProperty("profile_nickname")
    private String profileNickname;

    @JsonProperty("profile_thumbnail_image")
    private String profileThumbnailImage;

    @Getter
    @NoArgsConstructor
    public static class FriendsResponse {
        @JsonProperty("elements")
        private List<KakaoFriendResponse> elements;

        @JsonProperty("total_count")
        private Integer totalCount;
    }
}
