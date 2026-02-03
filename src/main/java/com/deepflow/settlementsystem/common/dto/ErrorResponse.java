package com.deepflow.settlementsystem.common.dto;

import com.deepflow.settlementsystem.common.code.ErrorCode;
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

    public ErrorResponse(ErrorCode errorCode){
        this.status = errorCode.getHttpStatus().value();
        this.message = errorCode.getMessage();
    }
}
