package com.github.marcel615.askteacher.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    //중복검증
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    //입력검증
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    //기타
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    //getter
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatus(){
        return httpStatus.value();
    }

    public String getMessage() {
        return message;
    }
}
