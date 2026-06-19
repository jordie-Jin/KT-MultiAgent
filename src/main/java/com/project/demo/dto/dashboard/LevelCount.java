package com.project.demo.dto.dashboard;

/** 로그 수준 분포 항목 (명세 §3 levelDistribution). */
public record LevelCount(String logLevel, long count) {
}
