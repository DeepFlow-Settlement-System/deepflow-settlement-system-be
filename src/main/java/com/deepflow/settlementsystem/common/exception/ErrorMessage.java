package com.deepflow.settlementsystem.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    // 404 Not Found
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 400 Bad Request
    INVALID_INPUT("입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD("필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden

    // 409 Conflict
    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;
}