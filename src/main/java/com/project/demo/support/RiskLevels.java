package com.project.demo.support;

/** 위험도(LOW/MEDIUM/HIGH/CRITICAL) ↔ 심각도 순서값 매핑 (명세 §9-2). */
public final class RiskLevels {

    private RiskLevels() {
    }

    /** 클러스터 최고 위험도 집계용 ordinal. 미분석/0 이면 null. */
    public static String fromOrdinal(Integer ordinal) {
        if (ordinal == null) {
            return null;
        }
        return switch (ordinal) {
            case 1 -> "LOW";
            case 2 -> "MEDIUM";
            case 3 -> "HIGH";
            case 4 -> "CRITICAL";
            default -> null;
        };
    }
}
