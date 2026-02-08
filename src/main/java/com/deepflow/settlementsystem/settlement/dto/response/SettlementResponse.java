package com.deepflow.settlementsystem.settlement.dto.response;

import com.deepflow.settlementsystem.expense.entity.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementResponse {
    private Long allocationId;
    private Long groupId;
    private String groupName;
    private Long expenseId;
    private String expenseTitle;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private String receiverNickname;
    private Long amount;
    private SettlementStatus status;
    private LocalDateTime createdAt;
}
