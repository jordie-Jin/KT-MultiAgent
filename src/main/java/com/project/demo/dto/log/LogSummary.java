package com.project.demo.dto.log;

import java.time.LocalDateTime;

/**
 * 로그 목록 행 (명세 §4.1 LogSummary).
 * 파생 필드:
 *  - isCaution  = label 이 alert(`!= '-'`)이면 true (주의 로그 판정, 라벨 기준)
 *  - isAnalysis = log_analysis row 존재(분석 완료, 주의와 독립)
 *  - riskLevel  = log_analysis 조인 결과(미분석 시 null)
 */
public record LogSummary(
        Long logId,
        LocalDateTime occurredAt,
        String node,
        String component,
        String logType,
        String logLevel,
        String label,
        boolean isCaution,
        boolean isAnalysis,
        String content,
        String riskLevel
) {
    /** JPQL 생성자 표현식용 — 원시 컬럼(label, analysisId)에서 파생 필드를 계산. */
    public LogSummary(
            Long logId,
            LocalDateTime occurredAt,
            String node,
            String component,
            String logType,
            String logLevel,
            String label,
            String content,
            String riskLevel,
            Long analysisId) {
        this(logId, occurredAt, node, component, logType, logLevel, label,
                label != null && !"-".equals(label),
                analysisId != null,
                content, riskLevel);
    }
}
