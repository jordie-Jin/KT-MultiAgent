package com.project.demo.repository;

import com.project.demo.dto.dashboard.RiskCount;
import com.project.demo.entity.LogAnalysis;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogAnalysisRepository extends JpaRepository<LogAnalysis, Long> {

    /** 범위 내 로그 중 분석 완료(log_analysis 존재) 수 (명세 §3 analyzedLogCount). */
    @Query("""
            SELECT COUNT(a) FROM LogAnalysis a
            WHERE a.log.logTs >= :startAt AND a.log.logTs <= :endAt
            """)
    long countAnalyzedInRange(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    /** 위험도 분포 (명세 §3 riskDistribution): 분석된 로그만 대상. */
    @Query("""
            SELECT new com.project.demo.dto.dashboard.RiskCount(a.riskLevel, COUNT(a))
            FROM LogAnalysis a
            WHERE a.log.logTs >= :startAt AND a.log.logTs <= :endAt
            GROUP BY a.riskLevel
            """)
    List<RiskCount> riskDistribution(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);
}
