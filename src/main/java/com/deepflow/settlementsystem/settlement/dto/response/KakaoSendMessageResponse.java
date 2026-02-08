package com.deepflow.settlementsystem.settlement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoSendMessageResponse {
    @JsonProperty("successful_receiver_uuids")
    private List<String> successfulReceiverUuids;
    
    @JsonProperty("failure_info")
    private List<FailureInfo> failureInfo;
    
    @Getter
    @NoArgsConstructor
    public static class FailureInfo {
        @JsonProperty("receiver_uuids")
        private List<String> receiverUuids;
        
        @JsonProperty("code")
        private Integer code;
        
        @JsonProperty("msg")
        private String msg;
    }
}
