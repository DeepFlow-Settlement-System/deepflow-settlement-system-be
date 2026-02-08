package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.expense.dto.ReceiptOcrResult;

public interface OcrClient {

    ReceiptOcrResult extract(String format, String name, byte[] imageBytes);
}
