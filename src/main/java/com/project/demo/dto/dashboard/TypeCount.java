package com.project.demo.dto.dashboard;

/** 로그 타입 분포 항목 (명세 §3 typeDistribution). */
public record TypeCount(String logType, long count) {
}
