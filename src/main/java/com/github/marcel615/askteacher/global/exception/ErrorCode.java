package com.github.marcel615.askteacher.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    //조회검증
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),

    //인증/인가 검증
    INVALID_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    POST_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "게시글 작성자만 수정할 수 있습니다."),

    //중복검증
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    //입력검증
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 형식입니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기 제한을 초과했습니다."),

    //기타
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다."),
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
