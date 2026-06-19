package com.project.demo.dto.pattern;

import java.time.LocalDateTime;

/** 패턴 상세의 관련 로그 요약 (명세 §6.2 relatedLogs). */
public record RelatedLog(
        Long logId,
        LocalDateTime occurredAt,
        String logLevel,
        String content
) {
}
