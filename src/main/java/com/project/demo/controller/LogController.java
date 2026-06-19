package com.project.demo.controller;

import com.project.demo.dto.common.PageResponse;
import com.project.demo.dto.log.LogDetail;
import com.project.demo.dto.log.LogSummary;
import com.project.demo.service.LogService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 로그 API (명세 §4). 주의 로그(§5)·분석 완료 목록은 isCaution/isAnalysis 필터로 흡수. */
@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public PageResponse<LogSummary> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isCaution,
            @RequestParam(required = false) Boolean isAnalysis,
            @PageableDefault(size = 50) Pageable pageable) {

        return logService.list(startAt, endAt, riskLevel, logType, component, logLevel,
                label, keyword, isCaution, isAnalysis, pageable);
    }

    @GetMapping("/{logId}")
    public LogDetail detail(@PathVariable Long logId) {
        return logService.detail(logId);
    }
}
