package com.deepflow.settlementsystem.expense.controller;

import com.deepflow.settlementsystem.expense.dto.OcrStatusResponse;
import com.deepflow.settlementsystem.expense.dto.ReceiptUploadResponse;
import com.deepflow.settlementsystem.expense.entity.Receipt;
import com.deepflow.settlementsystem.expense.repository.ReceiptRepository;
import com.deepflow.settlementsystem.expense.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipts")
@Tag(name = "Receipt", description = "영수증 업로드/OCR 조회 API")
@SecurityRequirement(name = "Authorization")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptRepository receiptRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "영수증 OCR 실행",
            description = "multipart/form-data로 image 파일을 업로드하면 비동기로 OCR을 요청하고, 등록한 영수증 이미지의 receiptId를 반환합니다."
    )
    public ResponseEntity<ReceiptUploadResponse> uploadReceipt(
            @Parameter(
                    description = "multipart/form-data로 업로드하는 영수증 이미지 파일",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("image") MultipartFile image
    ) {
        Long receiptId = receiptService.uploadReceipt(image);
        return ResponseEntity.ok(new ReceiptUploadResponse(receiptId));
    }

    @GetMapping("/{receiptId}/status")
    @Operation(
            summary = "영수증 OCR 상태 조회",
            description = "receiptId로 OCR 상태(예: PENDING/SUCCESS/FAILED)를 조회합니다."
    )
    public ResponseEntity<OcrStatusResponse> getOcrStatus(@PathVariable Long receiptId) {
        Receipt receipt = findReceipt(receiptId);
        return ResponseEntity.ok(new OcrStatusResponse(receipt.getOcrStatus()));
    }

    @GetMapping("/{receiptId}/analysis")
    @Operation(
            summary = "영수증 OCR 결과 조회",
            description = "OCR 성공(SUCCESS) 상태일 때만 결과 JSON을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OCR 결과 조회 성공(ReceiptOcrResult JSON 문자열)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "string", description = "ReceiptOcrResult JSON"),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "ReceiptOcrResult 예시",
                            value = "{\n  \"images\": [\n    {\n      \"inferResult\": \"SUCCESS\",\n      \"receipt\": {\n        \"result\": {\n          \"storeInfo\": {\n            \"name\": {\n              \"formatted\": {\n                \"value\": \"롯데리아\"\n              }\n            }\n          },\n          \"subResults\": [\n            {\n              \"items\": [\n                {\n                  \"name\": {\n                    \"formatted\": {\n                      \"value\": \"새우버거세트\"\n                    }\n                  },\n                  \"count\": {\n                    \"formatted\": {\n                      \"value\": \"1\"\n                    }\n                  },\n                  \"price\": {\n                    \"price\": {\n                      \"formatted\": {\n                        \"value\": \"5100\"\n                      }\n                    },\n                    \"unitPrice\": {\n                      \"formatted\": {\n                        \"value\": \"5100\"\n                      }\n                    }\n                  }\n                }\n              ]\n            }\n          ],\n          \"totalPrice\": {\n            \"price\": {\n              \"formatted\": {\n                \"value\": \"5100\"\n              }\n            }\n          }\n        }\n      }\n    }\n  ]\n}"
                    ))
    )
    public ResponseEntity<String> getOcrAnalysis(@PathVariable Long receiptId) {
        Receipt receipt = findReceipt(receiptId);
        if (!"SUCCESS".equalsIgnoreCase(receipt.getOcrStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "OCR is not successful");
        }
        if (receipt.getOcrResult() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OCR result not found");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(receipt.getOcrResult());
    }

    private Receipt findReceipt(Long receiptId) {
        return receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));
    }
}
