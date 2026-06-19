package com.project.demo.dto.dashboard;

/** 시계열 포인트 (명세 §3 timeSeries). time = 시간 버킷(ISO), totalCount/cautionCount 집계. */
public record TimePoint(String time, long totalCount, long cautionCount) {

    /** 네이티브 집계 row [bucket, total, caution] → DTO. */
    public static TimePoint fromRow(Object[] r) {
        return new TimePoint(
                (String) r[0],
                ((Number) r[1]).longValue(),
                r[2] != null ? ((Number) r[2]).longValue() : 0L);
    }
}
