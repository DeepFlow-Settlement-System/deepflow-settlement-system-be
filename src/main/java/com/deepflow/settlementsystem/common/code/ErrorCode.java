package com.deepflow.settlementsystem.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 404 Not Found
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GROUP_NOT_FOUND("그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND("방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 400 Bad Request
    INVALID_INPUT("입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_REQUIRED_FIELD("필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_INVALID_SIZE("입력값의 길이가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD("필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_INVITE_CODE("유효하지 않은 초대 코드입니다.", HttpStatus.BAD_REQUEST),
    INVITE_CODE_EXPIRED("초대 코드가 만료되었습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_MEMBER("이미 해당 그룹의 멤버입니다.", HttpStatus.BAD_REQUEST),
    NO_MEMBERS("방에 멤버가 없습니다.", HttpStatus.BAD_REQUEST),
    NO_EXPENSES("지출 내역이 없습니다.", HttpStatus.BAD_REQUEST),
    NO_SETTLEMENT("정산 결과가 없습니다. 먼저 정산을 계산해주세요.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    NOT_GROUP_MEMBER("해당 그룹의 멤버가 아닙니다.", HttpStatus.FORBIDDEN),
    NO_ACCESS_PERMISSION("해당 그룹에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 409 Conflict
    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),

    // 50x
    INTERNAL_SERVER_ERROR("서버 내부에 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVER_ERROR("외부 서버에 문제가 발생했습니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}