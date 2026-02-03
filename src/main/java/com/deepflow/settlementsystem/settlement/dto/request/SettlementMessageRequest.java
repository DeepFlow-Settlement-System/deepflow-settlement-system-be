package com.deepflow.settlementsystem.settlement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SettlementMessageRequest {

    @NotNull(message = "방 ID는 필수입니다.")
    private Long roomId;

    @NotEmpty(message = "메시지를 받을 사용자 ID 목록은 필수입니다.")
    private List<Long> receiverUserIds; // 메시지를 받을 사용자 ID 목록
}
