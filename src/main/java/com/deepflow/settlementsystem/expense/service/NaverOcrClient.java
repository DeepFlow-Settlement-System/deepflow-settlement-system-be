package com.deepflow.settlementsystem.expense.service;

import com.deepflow.settlementsystem.expense.dto.ReceiptOcrResult;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverOcrClient implements OcrClient {

    private static final String VERSION = "V2";

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${naver.ocr.url}")
    private String apiUrl;

    @Value("${naver.ocr.secret}")
    private String secretKey;

    @Override
    public ReceiptOcrResult extract(String format, String name, byte[] imageBytes) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes); // 이미지 인코딩

        Map<String, Object> payload = buildPayload(format, name, base64); // http body
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, buildHeaders()); // body + header

        // API 요청, dto로 받아오기
        ResponseEntity<ReceiptOcrResult> response = restTemplate.postForEntity(apiUrl, entity, ReceiptOcrResult.class);
        ReceiptOcrResult dto = response.getBody(); // 응답에서 body 가져오기

        return dto;
    }

    private HttpHeaders buildHeaders() { // 요청 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", secretKey);
        return headers;
    }

    private Map<String, Object> buildPayload(String format, String name, String base64) { // 요청 바디 생성
        Map<String, Object> image = new HashMap<>();
        image.put("format", format);
        image.put("name", name);
        image.put("data", base64);

        Map<String, Object> payload = new HashMap<>();
        payload.put("version", VERSION);
        payload.put("requestId", UUID.randomUUID().toString());
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("images", List.of(image));

        return payload;
    }

}
