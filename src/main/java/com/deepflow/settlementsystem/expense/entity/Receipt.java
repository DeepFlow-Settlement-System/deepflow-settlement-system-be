package com.deepflow.settlementsystem.expense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "image")
    private byte[] image; // 영수증 원본 이미지

    @Column(name = "ocr_status", length = 20)
    private String ocrStatus; // SUCCESS | FAILURE | ERROR | PENDING

    @Lob
    @Column(name = "ocr_result")
    private String ocrResult; // OCR 결과 JSON

    @OneToOne(mappedBy = "receipt")
    private Expense expense; // 이 영수증을 사진으로 가지는 expense
}
