package com.deepflow.settlementsystem.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReceiptUploadResponse {

    @Schema(description = "업로드된 영수증 ID", example = "1")
    private Long receiptId;
}
