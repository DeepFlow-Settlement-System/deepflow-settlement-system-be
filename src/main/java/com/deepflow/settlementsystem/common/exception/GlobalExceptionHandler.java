package com.deepflow.settlementsystem.common.exception;

import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customExceptionHandler(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(MethodArgumentNotValidException e) {
        FieldError firstError = (FieldError) e.getBindingResult().getAllErrors().get(0);
        String errorCode = firstError.getCode();
        
        ErrorCode errorCodeEnum = mapToErrorCode(errorCode);
        
        return ResponseEntity
                .status(errorCodeEnum.getHttpStatus())
                .body(new ErrorResponse(errorCodeEnum));
    }

    private ErrorCode mapToErrorCode(String errorCode) {
        return switch (errorCode) {
            case "NotBlank", "NotNull", "NotEmpty" -> ErrorCode.VALIDATION_REQUIRED_FIELD;
            case "Size", "Min", "Max" -> ErrorCode.VALIDATION_INVALID_SIZE;
            default -> ErrorCode.INVALID_INPUT;
        };
    }
}
