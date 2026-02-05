package com.deepflow.settlementsystem.common.exception;

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
                .status(e.getErrorMessage().getHttpStatus())
                .body(new ErrorResponse(e.getErrorMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(MethodArgumentNotValidException e) {
        FieldError firstError = (FieldError) e.getBindingResult().getAllErrors().get(0);
        String errorCode = firstError.getCode(); // "NotBlank", "Size" 등
        
        ErrorMessage errorMessage = mapToErrorMessage(errorCode);
        
        return ResponseEntity
                .status(errorMessage.getHttpStatus())
                .body(new ErrorResponse(errorMessage));
    }

    private ErrorMessage mapToErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "NotBlank", "NotNull", "NotEmpty" -> ErrorMessage.VALIDATION_REQUIRED_FIELD;
            case "Size", "Min", "Max" -> ErrorMessage.VALIDATION_INVALID_SIZE;
            default -> ErrorMessage.INVALID_INPUT;
        };
    }
}
