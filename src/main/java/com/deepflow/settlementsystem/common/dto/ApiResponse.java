package com.deepflow.settlementsystem.common.dto;

import com.deepflow.settlementsystem.common.code.ApiResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private Integer status;
    private String message;
    private T data;

    public ApiResponse(ApiResponseCode apiResponseCode, T data) {
        this.message = apiResponseCode.getMessage();
        this.status = apiResponseCode.getHttpStatus().value();
        this.data = data;
    }
}
