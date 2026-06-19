package com.project.demo.service;

import com.project.demo.dto.common.PageResponse;
import com.project.demo.dto.pattern.PatternDetail;
import com.project.demo.dto.pattern.PatternSummary;
import com.project.demo.dto.pattern.RelatedLog;
import com.project.demo.repository.PatternClusterRepository;
import com.project.demo.support.QuerySupport;
import com.project.demo.web.ApiException;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatternService {

    /** 관련 로그 기본 상한 (명세 §6.2: 필요 시 페이지네이션 지원). */
    private static final int RELATED_LOG_LIMIT = 100;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "importance");

    private final PatternClusterRepository patternRepository;

    public PatternService(PatternClusterRepository patternRepository) {
        this.patternRepository = patternRepository;
    }

    /** 패턴 목록 (명세 §6.1). 정렬 기본값 importance,desc. */
    public PageResponse<PatternSummary> list(String riskLevel, Pageable pageable) {
        Pageable paged = QuerySupport.withSort(pageable, Map.of(), DEFAULT_SORT);
        Page<PatternSummary> page = patternRepository.searchRaw(riskLevel, paged)
                .map(PatternSummary::fromRow);
        return PageResponse.from(page);
    }

    /** 패턴 상세 (명세 §6.2). */
    public PatternDetail detail(Long patternId) {
        List<Object[]> rows = patternRepository.findRowById(patternId);
        if (rows.isEmpty()) {
            throw ApiException.patternNotFound(patternId);
        }
        PatternSummary summary = PatternSummary.fromRow(rows.get(0));
        List<RelatedLog> relatedLogs = patternRepository.relatedLogs(
                patternId,
                PageRequest.of(0, RELATED_LOG_LIMIT));
        return PatternDetail.of(summary, relatedLogs);
    }

    /** 대시보드 recentPatterns 용 — 중요도 상위 N개. */
    public List<PatternSummary> top(int n) {
        return patternRepository.searchRaw(null, PageRequest.of(0, n, DEFAULT_SORT))
                .map(PatternSummary::fromRow)
                .getContent();
    }
}
