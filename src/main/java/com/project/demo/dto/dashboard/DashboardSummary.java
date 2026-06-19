package com.project.demo.dto.dashboard;

import com.project.demo.dto.log.LogSummary;
import com.project.demo.dto.pattern.PatternSummary;
import java.time.LocalDateTime;
import java.util.List;

/** 대시보드 통합 응답 (명세 §3.1). */
public record DashboardSummary(
        Range range,
        Stats stats,
        List<TimePoint> timeSeries,
        List<RiskCount> riskDistribution,
        List<TypeCount> typeDistribution,
        List<ComponentCount> componentDistribution,
        List<LevelCount> levelDistribution,
        List<LogSummary> recentCautionLogs,
        List<PatternSummary> recentPatterns
) {
    public record Range(LocalDateTime startAt, LocalDateTime endAt) {
    }

    /**
     * cautionLogCount = label alert(주의) 로그 수, analyzedLogCount = 분석 완료 로그 수(주의와 독립).
     */
    public record Stats(long totalLogCount, long cautionLogCount, long analyzedLogCount) {
    }
}
