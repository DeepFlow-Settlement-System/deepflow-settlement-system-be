package com.deepflow.settlementsystem.kakao.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoMessageRequest {

    @JsonProperty("receiver_uuids")
    private String[] receiverUuids; // 메시지 받을 친구 UUID 배열

    @JsonProperty("template_object")
    private TemplateObject templateObject;

    @Getter
    @Builder
    public static class TemplateObject {
        @JsonProperty("object_type")
        @Builder.Default
        private String objectType = "text";

        private String text;

        private Link link;

        @Getter
        @Builder
        public static class Link {
            @JsonProperty("web_url")
            private String webUrl;

            @JsonProperty("mobile_web_url")
            private String mobileWebUrl;
        }
    }
}
