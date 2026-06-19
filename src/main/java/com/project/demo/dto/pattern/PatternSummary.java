package com.project.demo.dto.pattern;

import com.project.demo.support.RiskLevels;

/**
 * 패턴 카드 (명세 §6.1). count/riskLevel 은 소속 log_analysis 집계 파생값.
 * riskLevel = 소속 분석들의 최고 위험도(없으면 null).
 */
public record PatternSummary(
        Long patternId,
        String patternName,
        String description,
        String representativeLog,
        Integer importance,
        long count,
        String riskLevel
) {
    /** 집계 쿼리 row [id, title, description, eventTemplate, importance, count, maxRiskOrdinal] → DTO. */
    public static PatternSummary fromRow(Object[] r) {
        Long id = ((Number) r[0]).longValue();
        Integer importance = r[4] != null ? ((Number) r[4]).intValue() : null;
        long count = r[5] != null ? ((Number) r[5]).longValue() : 0L;
        Integer ordinal = r[6] != null ? ((Number) r[6]).intValue() : null;
        return new PatternSummary(
                id,
                (String) r[1],
                (String) r[2],
                (String) r[3],
                importance,
                count,
                RiskLevels.fromOrdinal(ordinal));
    }
}
