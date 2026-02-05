package com.deepflow.settlementsystem.common.exception;

import com.deepflow.settlementsystem.common.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    private ErrorCode errorCode;
}
