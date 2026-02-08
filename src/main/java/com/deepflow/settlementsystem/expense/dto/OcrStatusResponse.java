package com.deepflow.settlementsystem.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OcrStatusResponse {

    @Schema(description = "OCR 처리 상태", example = "PENDING")
    private String ocrStatus;
}
