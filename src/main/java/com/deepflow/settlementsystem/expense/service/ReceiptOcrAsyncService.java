package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.expense.dto.ReceiptOcrResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deepflow.settlementsystem.expense.entity.Receipt;
import com.deepflow.settlementsystem.expense.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOcrAsyncService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OcrClient ocrClient;
    private final ReceiptRepository receiptRepository;

    @Async
    @Transactional
    public void processReceipt(Long receiptId, String format, String name, byte[] imageBytes) {
        try {
            ReceiptOcrResult result = ocrClient.extract(format, name, imageBytes);
            saveOcrResult(receiptId, result);

        } catch (Exception ex) {
            log.warn("OCR processing failed. receiptId={}", receiptId, ex);
        }
    }

    // OCR 결과를 DB에 저장 (receipt.ocrResult)
    private void saveOcrResult(Long receiptId, ReceiptOcrResult ocrResult) {
      Receipt receipt = receiptRepository.findById(receiptId)
              .orElseThrow(() -> new IllegalStateException("Receipt not found: " + receiptId));

      // OCR 결과(상태) 저장
      String inferResult = ocrResult.images().get(0).inferResult();
      receipt.setOcrStatus(inferResult);

      // OCR 반환 값 저장 (ReceiptOcrResult JSON 그대로)
      receipt.setOcrResult(toJson(ocrResult));
    }

    private String toJson(ReceiptOcrResult ocrResult) {
        try {
            return objectMapper.writeValueAsString(ocrResult);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize OCR result", e);
            return null;
        }
    }
}
