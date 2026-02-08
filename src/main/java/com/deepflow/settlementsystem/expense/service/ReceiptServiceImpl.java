package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.expense.entity.Receipt;
import com.deepflow.settlementsystem.expense.repository.ReceiptRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
// 사용자로부터 받은 영수증 이미지 db저장 후, 비동기 OCR 호출
public class ReceiptServiceImpl implements ReceiptService {

    private static final String OCR_STATUS_PENDING = "PENDING";

    private final ReceiptRepository receiptRepository;
    private final ReceiptOcrAsyncService receiptOcrAsyncService;

    @Override
    @Transactional
    // 사용자가 영수증 이미지를 업로드하면, OCR로 결과 반환 후 receipt 테이블 저장(OCR 결과포함)
    public Long uploadReceipt(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt image is required");
        }
        Receipt receipt = new Receipt();

        try {
            receipt.setImage(image.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read image");
        }

        // OCR 요청 전 상태
        receipt.setOcrStatus(OCR_STATUS_PENDING);
        receipt = receiptRepository.save(receipt);

        String format = resolveFormat(image);
        String imageName = String.valueOf(receipt.getId());

        // 비동기 OCR 호출 (현재는 Stub)
        receiptOcrAsyncService.processReceipt(receipt.getId(), format, imageName, receipt.getImage());
        return receipt.getId();
    }

    private String resolveFormat(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType != null) {
            if (contentType.equalsIgnoreCase("image/jpeg")) {
                return "jpeg";
            }
            if (contentType.equalsIgnoreCase("image/jpg")) {
                return "jpg";
            }
            if (contentType.equalsIgnoreCase("image/png")) {
                return "png";
            }
        }

        String filename = image.getOriginalFilename();
        if (filename != null) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1).toLowerCase();
            }
        }

        // 기본값은 jpeg로 처리
        return "jpeg";
    }
}
