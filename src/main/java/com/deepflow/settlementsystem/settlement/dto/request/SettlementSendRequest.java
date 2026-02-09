package com.deepflow.settlementsystem.settlement.dto.request;

import com.deepflow.settlementsystem.expense.entity.SettlementStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SettlementSendRequest {
    
    @NotNull
    private Long targetUserId;
    
    private List<SettlementStatus> statuses;
}
