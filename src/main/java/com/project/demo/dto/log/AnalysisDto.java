package com.project.demo.dto.log;

import com.project.demo.entity.LogAnalysis;
import com.project.demo.support.ActionParser;
import java.util.List;

/** 로그 상세의 AI 분석 결과 (명세 §4.2 analysis). */
public record AnalysisDto(
        Long analysisId,
        String domain,
        String riskLevel,
        String aiSummary,
        String analysis,
        List<String> responsePlan
) {
    public static AnalysisDto of(LogAnalysis a) {
        return new AnalysisDto(
                a.getId(),
                a.getDomain(),
                a.getRiskLevel(),
                a.getSummary(),
                a.getAnalysis(),
                ActionParser.parse(a.getAction()));
    }
}
