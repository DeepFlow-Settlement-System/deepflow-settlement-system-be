package com.deepflow.settlementsystem.settlement.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementSummaryResponse {
    private Long targetUserId;
    private String targetUserNickname;
    private Long totalUnsettledAmount;
    private Long totalRequestedAmount;
    private Long totalCompletedAmount;
    private Long totalAmount;
}
