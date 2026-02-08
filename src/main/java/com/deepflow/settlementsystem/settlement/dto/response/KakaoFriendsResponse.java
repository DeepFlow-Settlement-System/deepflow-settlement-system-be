package com.deepflow.settlementsystem.settlement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoFriendsResponse {
    @JsonProperty("elements")
    private List<Friend> elements;
    
    @JsonProperty("total_count")
    private Integer totalCount;
    
    @JsonProperty("after_url")
    private String afterUrl;
    
    @JsonProperty("favorite_count")
    private Integer favoriteCount;
    
    @Getter
    @NoArgsConstructor
    public static class Friend {
        @JsonProperty("profile_nickname")
        private String profileNickname;
        
        @JsonProperty("profile_thumbnail_image")
        private String profileThumbnailImage;
        
        @JsonProperty("allowed_msg")
        private Boolean allowedMsg;
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("uuid")
        private String uuid;
        
        @JsonProperty("favorite")
        private Boolean favorite;
    }
}
