package com.project.demo.dto.log;

import com.project.demo.entity.PatternCluster;

/** 로그 상세에서 참조하는 매핑 패턴 (명세 §4.2 pattern). */
public record PatternRef(
        Long patternId,
        String patternName,
        String representativeLog
) {
    public static PatternRef of(PatternCluster c) {
        return new PatternRef(c.getId(), c.getTitle(), c.getEventTemplate());
    }
}
