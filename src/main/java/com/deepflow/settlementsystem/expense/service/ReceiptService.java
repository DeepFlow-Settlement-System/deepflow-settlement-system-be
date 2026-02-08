package com.deepflow.settlementsystem.expense.service;

import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

    Long uploadReceipt(MultipartFile image);
}
