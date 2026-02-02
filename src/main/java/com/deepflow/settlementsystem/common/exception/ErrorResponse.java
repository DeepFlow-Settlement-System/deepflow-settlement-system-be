package com.deepflow.settlementsystem.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private Integer status;
    private String message;

    ErrorResponse(ErrorMessage errorMessage){
        this.status = errorMessage.getHttpStatus().value();
        this.message = errorMessage.getMessage();
    }
}
