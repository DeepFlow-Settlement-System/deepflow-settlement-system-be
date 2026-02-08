package com.deepflow.settlementsystem.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SettlementItem {
    private String description;
    private Long amount;
}
