package com.deepflow.settlementsystem.settlement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettlementSendRequest {
    
    @NotNull
    private Long allocationId;
}
