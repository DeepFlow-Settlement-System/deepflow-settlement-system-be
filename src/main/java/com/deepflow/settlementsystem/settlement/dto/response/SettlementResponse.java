package com.deepflow.settlementsystem.settlement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementResponse {
    private Long id;
    private Long roomId;
    private Long userId;
    private Long receiveAmount;
    private Long sendAmount;
    private Long netAmount; // 받을 금액 - 보낼 금액 (양수면 받을 금액, 음수면 보낼 금액)
    private LocalDateTime calculatedAt;
    private LocalDateTime updatedAt;
}
