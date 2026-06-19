package com.project.demo.dto.log;

import com.project.demo.entity.BglLog;
import java.time.LocalDateTime;

/** 로그 상세의 원본 로그 전문 (명세 §4.2 log). */
public record LogRaw(
        Long logId,
        LocalDateTime occurredAt,
        String status,
        String node,
        String nodeRepeat,
        String component,
        String logType,
        String logLevel,
        String label,
        String eventId,
        boolean isCaution,
        boolean isAnalysis,
        String content
) {
    public static LogRaw of(BglLog e, boolean isAnalysis) {
        boolean caution = e.getLabel() != null && !"-".equals(e.getLabel());
        return new LogRaw(
                e.getId(),
                e.getLogTs(),
                e.getStatus(),
                e.getNode(),
                e.getNodeRepeat(),
                e.getComponent(),
                e.getLogType(),
                e.getLogLevel(),
                e.getLabel(),
                e.getEventId(),
                caution,
                isAnalysis,
                e.getContent());
    }
}
