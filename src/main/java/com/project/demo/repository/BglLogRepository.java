package com.project.demo.repository;

import com.project.demo.dto.dashboard.ComponentCount;
import com.project.demo.dto.dashboard.LevelCount;
import com.project.demo.dto.dashboard.TypeCount;
import com.project.demo.dto.log.LogSummary;
import com.project.demo.entity.BglLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BglLogRepository extends JpaRepository<BglLog, Long> {

    /**
     * 로그 목록 조회 (명세 §4.1). bgl_log 주체 + log_analysis LEFT JOIN 단일 projection.
     * 모든 필터는 nullable — null 이면 해당 조건 무시.
     * - isCaution : label alert(`!= '-'`) 여부 (라벨 기준 주의 로그)
     * - isAnalysis: log_analysis row 존재 여부 (분석 완료, 주의와 독립)
     */
    @Query(value = """
            SELECT new com.project.demo.dto.log.LogSummary(
                l.id, l.logTs, l.node, l.component, l.logType, l.logLevel, l.label, l.content, a.riskLevel, a.id)
            FROM BglLog l
            LEFT JOIN LogAnalysis a ON a.log = l
            WHERE (:startAt IS NULL OR l.logTs >= :startAt)
              AND (:endAt   IS NULL OR l.logTs <= :endAt)
              AND (:logType   IS NULL OR l.logType = :logType)
              AND (:component IS NULL OR l.component = :component)
              AND (:logLevel  IS NULL OR l.logLevel = :logLevel)
              AND (:label     IS NULL OR l.label = :label)
              AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)
              AND (:keyword   IS NULL OR LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isCaution IS NULL
                   OR (:isCaution = TRUE  AND l.label IS NOT NULL AND l.label <> '-')
                   OR (:isCaution = FALSE AND (l.label IS NULL OR l.label = '-')))
              AND (:isAnalysis IS NULL
                   OR (:isAnalysis = TRUE  AND a.id IS NOT NULL)
                   OR (:isAnalysis = FALSE AND a.id IS NULL))
            """,
            countQuery = """
            SELECT COUNT(l)
            FROM BglLog l
            LEFT JOIN LogAnalysis a ON a.log = l
            WHERE (:startAt IS NULL OR l.logTs >= :startAt)
              AND (:endAt   IS NULL OR l.logTs <= :endAt)
              AND (:logType   IS NULL OR l.logType = :logType)
              AND (:component IS NULL OR l.component = :component)
              AND (:logLevel  IS NULL OR l.logLevel = :logLevel)
              AND (:label     IS NULL OR l.label = :label)
              AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)
              AND (:keyword   IS NULL OR LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isCaution IS NULL
                   OR (:isCaution = TRUE  AND l.label IS NOT NULL AND l.label <> '-')
                   OR (:isCaution = FALSE AND (l.label IS NULL OR l.label = '-')))
              AND (:isAnalysis IS NULL
                   OR (:isAnalysis = TRUE  AND a.id IS NOT NULL)
                   OR (:isAnalysis = FALSE AND a.id IS NULL))
            """)
    Page<LogSummary> search(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("logType") String logType,
            @Param("component") String component,
            @Param("logLevel") String logLevel,
            @Param("label") String label,
            @Param("riskLevel") String riskLevel,
            @Param("keyword") String keyword,
            @Param("isCaution") Boolean isCaution,
            @Param("isAnalysis") Boolean isAnalysis,
            Pageable pageable);

    /**
     * 로그 상세 단일 조회 (명세 §4.2 / §11.1). bgl_log 1건을 log_analysis·pattern_cluster 와
     * LEFT JOIN 하여 1 round-trip 으로 조회. 결과 row = [BglLog, LogAnalysis|null, PatternCluster|null].
     */
    @Query("""
            SELECT l, a, c
            FROM BglLog l
            LEFT JOIN LogAnalysis a ON a.log = l
            LEFT JOIN PatternCluster c ON c = a.cluster
            WHERE l.id = :id
            """)
    List<Object[]> findDetailById(@Param("id") Long id);

    // ── 대시보드 집계 (명세 §3) ────────────────────────────────────────────

    @Query("SELECT COUNT(l) FROM BglLog l WHERE l.logTs >= :startAt AND l.logTs <= :endAt")
    long countInRange(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query("""
            SELECT COUNT(l) FROM BglLog l
            WHERE l.logTs >= :startAt AND l.logTs <= :endAt
              AND l.label IS NOT NULL AND l.label <> '-'
            """)
    long countCautionInRange(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query("""
            SELECT new com.project.demo.dto.dashboard.TypeCount(l.logType, COUNT(l))
            FROM BglLog l
            WHERE l.logTs >= :startAt AND l.logTs <= :endAt
            GROUP BY l.logType
            ORDER BY COUNT(l) DESC
            """)
    List<TypeCount> typeDistribution(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query("""
            SELECT new com.project.demo.dto.dashboard.ComponentCount(l.component, COUNT(l))
            FROM BglLog l
            WHERE l.logTs >= :startAt AND l.logTs <= :endAt
            GROUP BY l.component
            ORDER BY COUNT(l) DESC
            """)
    List<ComponentCount> componentDistribution(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Query("""
            SELECT new com.project.demo.dto.dashboard.LevelCount(l.logLevel, COUNT(l))
            FROM BglLog l
            WHERE l.logTs >= :startAt AND l.logTs <= :endAt
            GROUP BY l.logLevel
            ORDER BY COUNT(l) DESC
            """)
    List<LevelCount> levelDistribution(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    /** 시간(1h) 버킷별 총/주의 로그 수. MySQL DATE_FORMAT 사용. 결과 row = [bucketIso, total, caution]. */
    @Query(value = """
            SELECT DATE_FORMAT(log_ts, '%Y-%m-%dT%H:00:00') AS bucket,
                   COUNT(*) AS total,
                   SUM(CASE WHEN label IS NOT NULL AND label <> '-' THEN 1 ELSE 0 END) AS caution
            FROM bgl_log
            WHERE log_ts >= :startAt AND log_ts <= :endAt
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> hourlySeries(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);
}
