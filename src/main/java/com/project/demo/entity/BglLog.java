package com.project.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원시 로그 테이블 (bgl_log).
 * 분석 엔진이 적재하고 백엔드는 조회한다.
 */
@Entity
@Table(name = "bgl_log")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BglLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String status;

    @Column(length = 20)
    private String label;

    /** 발생 노드 */
    @Column(length = 100)
    private String node;

    /** 보고 노드 */
    @Column(name = "node_repeat", length = 100)
    private String nodeRepeat;

    @Column(length = 100)
    private String component;

    @Column(name = "log_type", length = 50)
    private String logType;

    /** 타임스탬프(Time) */
    @Column(name = "log_ts")
    private LocalDateTime logTs;

    @Column(name = "log_level", length = 10)
    private String logLevel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "event_id", length = 50)
    private String eventId;
}
