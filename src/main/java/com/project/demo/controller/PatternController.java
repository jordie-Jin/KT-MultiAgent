package com.project.demo.controller;

import com.project.demo.dto.common.PageResponse;
import com.project.demo.dto.pattern.PatternDetail;
import com.project.demo.dto.pattern.PatternSummary;
import com.project.demo.service.PatternService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 패턴(로그 군집) API (명세 §6). */
@RestController
@RequestMapping("/api/v1/log-patterns")
public class PatternController {

    private final PatternService patternService;

    public PatternController(PatternService patternService) {
        this.patternService = patternService;
    }

    @GetMapping
    public PageResponse<PatternSummary> list(
            @RequestParam(required = false) String riskLevel,
            @PageableDefault(size = 20, sort = "importance", direction = Sort.Direction.DESC) Pageable pageable) {
        return patternService.list(riskLevel, pageable);
    }

    @GetMapping("/{patternId}")
    public PatternDetail detail(@PathVariable Long patternId) {
        return patternService.detail(patternId);
    }
}
