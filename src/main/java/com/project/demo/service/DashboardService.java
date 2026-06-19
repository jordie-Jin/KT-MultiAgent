package com.project.demo.service;

import com.project.demo.dto.dashboard.DashboardSummary;
import com.project.demo.dto.dashboard.TimePoint;
import com.project.demo.dto.log.LogSummary;
import com.project.demo.repository.BglLogRepository;
import com.project.demo.repository.LogAnalysisRepository;
import com.project.demo.support.QuerySupport;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_CAUTION_LIMIT = 5;
    private static final int RECENT_PATTERN_LIMIT = 5;

    private final BglLogRepository logRepository;
    private final LogAnalysisRepository analysisRepository;
    private final PatternService patternService;

    public DashboardService(BglLogRepository logRepository,
                            LogAnalysisRepository analysisRepository,
                            PatternService patternService) {
        this.logRepository = logRepository;
        this.analysisRepository = analysisRepository;
        this.patternService = patternService;
    }

    /** 대시보드 통합 조회 (명세 §3.1). interval 은 현재 1h 고정(향후 확장). */
    public DashboardSummary summary(LocalDateTime startAt, LocalDateTime endAt, String interval) {
        LocalDateTime[] r = QuerySupport.resolveRange(startAt, endAt);
        LocalDateTime start = r[0];
        LocalDateTime end = r[1];

        var stats = new DashboardSummary.Stats(
                logRepository.countInRange(start, end),
                logRepository.countCautionInRange(start, end),
                analysisRepository.countAnalyzedInRange(start, end));

        List<TimePoint> timeSeries = logRepository.hourlySeries(start, end).stream()
                .map(TimePoint::fromRow)
                .toList();

        List<LogSummary> recentCautionLogs = logRepository.search(
                start, end, null, null, null, null, null, null, Boolean.TRUE, null,
                PageRequest.of(0, RECENT_CAUTION_LIMIT, Sort.by(Sort.Direction.DESC, "logTs")))
                .getContent();

        return new DashboardSummary(
                new DashboardSummary.Range(start, end),
                stats,
                timeSeries,
                analysisRepository.riskDistribution(start, end),
                logRepository.typeDistribution(start, end),
                logRepository.componentDistribution(start, end),
                logRepository.levelDistribution(start, end),
                recentCautionLogs,
                patternService.top(RECENT_PATTERN_LIMIT));
    }
}
