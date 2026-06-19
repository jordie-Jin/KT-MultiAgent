package com.project.demo.dto.common;

/** API 명세 §1 공통 에러 포맷. */
public record ApiError(
        String timestamp,
        int status,
        String code,
        String message,
        String path
) {
}
