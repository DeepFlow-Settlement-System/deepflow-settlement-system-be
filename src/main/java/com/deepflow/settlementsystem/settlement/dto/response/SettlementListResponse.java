package com.deepflow.settlementsystem.settlement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SettlementListResponse {
    private Long roomId;
    private List<SettlementResponse> settlements;
}
