package com.project.demo.web;

import org.springframework.http.HttpStatus;

/** 도메인 에러를 명세 §1 의 code/status 로 표현하기 위한 예외. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static ApiException logNotFound(Long logId) {
        return new ApiException(HttpStatus.NOT_FOUND, "LOG_NOT_FOUND",
                "존재하지 않는 logId 입니다: " + logId);
    }

    public static ApiException patternNotFound(Long patternId) {
        return new ApiException(HttpStatus.NOT_FOUND, "PATTERN_NOT_FOUND",
                "존재하지 않는 patternId 입니다: " + patternId);
    }

    public static ApiException invalidDateRange() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE",
                "startAt은 endAt보다 이전이어야 합니다.");
    }
}
