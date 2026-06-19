package com.project.demo.service;

import com.project.demo.dto.common.PageResponse;
import com.project.demo.dto.log.AnalysisDto;
import com.project.demo.dto.log.LogDetail;
import com.project.demo.dto.log.LogRaw;
import com.project.demo.dto.log.LogSummary;
import com.project.demo.dto.log.PatternRef;
import com.project.demo.entity.BglLog;
import com.project.demo.entity.LogAnalysis;
import com.project.demo.entity.PatternCluster;
import com.project.demo.repository.BglLogRepository;
import com.project.demo.support.QuerySupport;
import com.project.demo.web.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogService {

    private static final Map<String, String> SORT_ALIAS = Map.of("occurredAt", "logTs");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "logTs");

    private final BglLogRepository logRepository;

    public LogService(BglLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /** 로그 목록 조회 (명세 §4.1). */
    public PageResponse<LogSummary> list(
            LocalDateTime startAt,
            LocalDateTime endAt,
            String riskLevel,
            String logType,
            String component,
            String logLevel,
            String label,
            String keyword,
            Boolean isCaution,
            Boolean isAnalysis,
            Pageable pageable) {

        LocalDateTime[] range = QuerySupport.resolveRange(startAt, endAt);
        Pageable paged = QuerySupport.withSort(pageable, SORT_ALIAS, DEFAULT_SORT);

        Page<LogSummary> page = logRepository.search(
                range[0], range[1], logType, component, logLevel, label, riskLevel,
                emptyToNull(keyword), isCaution, isAnalysis, paged);

        return PageResponse.from(page);
    }

    /** 로그 상세 조회 (명세 §4.2 / §11.1): bgl_log 주체 단일 LEFT JOIN. */
    public LogDetail detail(Long logId) {
        List<Object[]> rows = logRepository.findDetailById(logId);
        if (rows.isEmpty()) {
            throw ApiException.logNotFound(logId);
        }
        Object[] row = rows.get(0);
        BglLog log = (BglLog) row[0];
        LogAnalysis analysis = (LogAnalysis) row[1];
        PatternCluster cluster = (PatternCluster) row[2];

        boolean analyzed = analysis != null;
        LogRaw raw = LogRaw.of(log, analyzed);
        AnalysisDto analysisDto = analyzed ? AnalysisDto.of(analysis) : null;
        PatternRef patternRef = cluster != null ? PatternRef.of(cluster) : null;

        return new LogDetail(raw, analysisDto, patternRef);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
