package com.deepflow.settlementsystem.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiResponseCode {
    OK("요청이 정상 처리되었습니다.", HttpStatus.OK);

    private final String message;
    private final HttpStatus httpStatus;
}
