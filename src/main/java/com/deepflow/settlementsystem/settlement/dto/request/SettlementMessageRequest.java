package com.deepflow.settlementsystem.settlement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettlementMessageRequest {
    
    @NotNull
    private Long receiverUserId;
    
    @NotNull
    @Positive
    private Long amount;
}
