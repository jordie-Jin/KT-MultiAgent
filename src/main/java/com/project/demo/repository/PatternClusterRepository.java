package com.project.demo.repository;

import com.project.demo.dto.pattern.RelatedLog;
import com.project.demo.entity.PatternCluster;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatternClusterRepository extends JpaRepository<PatternCluster, Long> {

    /**
     * 패턴 목록 (명세 §6.1). 클러스터별 집계(count, 최고 위험도 ordinal)를 상관 서브쿼리로 계산.
     * 결과 row = [id, title, description, eventTemplate, importance, count, maxRiskOrdinal].
     * riskLevel 필터: 해당 위험도 분석이 1건 이상 존재하는 클러스터만.
     */
    @Query(value = """
            SELECT c.id, c.title, c.description, c.eventTemplate, c.importance,
                   (SELECT COUNT(a) FROM LogAnalysis a WHERE a.cluster = c),
                   (SELECT MAX(CASE a.riskLevel
                                  WHEN 'LOW' THEN 1 WHEN 'MEDIUM' THEN 2
                                  WHEN 'HIGH' THEN 3 WHEN 'CRITICAL' THEN 4 ELSE 0 END)
                      FROM LogAnalysis a WHERE a.cluster = c)
            FROM PatternCluster c
            WHERE (:riskLevel IS NULL
                   OR EXISTS (SELECT 1 FROM LogAnalysis a2 WHERE a2.cluster = c AND a2.riskLevel = :riskLevel))
            """,
            countQuery = """
            SELECT COUNT(c) FROM PatternCluster c
            WHERE (:riskLevel IS NULL
                   OR EXISTS (SELECT 1 FROM LogAnalysis a2 WHERE a2.cluster = c AND a2.riskLevel = :riskLevel))
            """)
    Page<Object[]> searchRaw(@Param("riskLevel") String riskLevel, Pageable pageable);

    /** 단일 패턴 집계 row (명세 §6.2). */
    @Query("""
            SELECT c.id, c.title, c.description, c.eventTemplate, c.importance,
                   (SELECT COUNT(a) FROM LogAnalysis a WHERE a.cluster = c),
                   (SELECT MAX(CASE a.riskLevel
                                  WHEN 'LOW' THEN 1 WHEN 'MEDIUM' THEN 2
                                  WHEN 'HIGH' THEN 3 WHEN 'CRITICAL' THEN 4 ELSE 0 END)
                      FROM LogAnalysis a WHERE a.cluster = c)
            FROM PatternCluster c
            WHERE c.id = :id
            """)
    List<Object[]> findRowById(@Param("id") Long id);

    /** 패턴 관련 로그 (명세 §6.2): pattern_cluster → log_analysis(cluster_id) → bgl_log(log_id). */
    @Query("""
            SELECT new com.project.demo.dto.pattern.RelatedLog(l.id, l.logTs, l.logLevel, l.content)
            FROM LogAnalysis a
            JOIN a.log l
            WHERE a.cluster.id = :patternId
            ORDER BY l.logTs DESC
            """)
    List<RelatedLog> relatedLogs(@Param("patternId") Long patternId, Pageable pageable);
}
