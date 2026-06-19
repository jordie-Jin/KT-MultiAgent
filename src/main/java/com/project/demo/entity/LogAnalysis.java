package com.project.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그 AI 진단/분석 결과 (log_analysis).
 * 원시 로그(bgl_log)와 패턴 클러스터(pattern_cluster)를 참조한다.
 */
@Entity
@Table(name = "log_analysis")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LogAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 원시 로그 참조 (FK → bgl_log.id) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "log_id", nullable = false)
    private BglLog log;

    /** 로그 도메인 (ENUM: BGL) */
    @Column(length = 20)
    private String domain;

    /** 위험도 */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    /** 요약 */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 분석 */
    @Column(columnDefinition = "TEXT")
    private String analysis;

    /** 대응 방안 */
    @Column(columnDefinition = "TEXT")
    private String action;

    /** 클러스터 참조 (FK → pattern_cluster.id) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id", nullable = false)
    private PatternCluster cluster;
}
