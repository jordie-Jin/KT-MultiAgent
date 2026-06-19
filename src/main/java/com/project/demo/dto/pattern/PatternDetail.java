package com.project.demo.dto.pattern;

import java.util.List;

/** 패턴 상세 (명세 §6.2): 요약 + 관련 로그 목록. */
public record PatternDetail(
        Long patternId,
        String patternName,
        String description,
        String representativeLog,
        Integer importance,
        long count,
        String riskLevel,
        List<RelatedLog> relatedLogs
) {
    public static PatternDetail of(PatternSummary s, List<RelatedLog> relatedLogs) {
        return new PatternDetail(
                s.patternId(), s.patternName(), s.description(), s.representativeLog(),
                s.importance(), s.count(), s.riskLevel(), relatedLogs);
    }
}
