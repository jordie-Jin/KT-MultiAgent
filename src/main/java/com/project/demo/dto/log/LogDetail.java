package com.project.demo.dto.log;

/** 로그 상세 (명세 §4.2). analysis/pattern 은 미분석 시 null. */
public record LogDetail(
        LogRaw log,
        AnalysisDto analysis,
        PatternRef pattern
) {
}
