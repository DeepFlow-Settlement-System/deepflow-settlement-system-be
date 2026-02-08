package com.deepflow.settlementsystem.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupExpenseTotalResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "그룹 총 지출 합계", example = "12000")
    private Long totalAmount;
}
