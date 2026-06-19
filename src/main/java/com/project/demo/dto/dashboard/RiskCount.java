package com.project.demo.dto.dashboard;

/** 위험도 분포 항목 (명세 §3 riskDistribution). 분석된 로그만 집계. */
public record RiskCount(String riskLevel, long count) {
}
