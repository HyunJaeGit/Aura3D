package com.aura.assistant.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * MonitoringHistory 엔티티
 * 프로젝트의 상태 체크 이력을 개별적으로 저장하는 클래스입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MonitoringHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 프로젝트의 기록인지 연결 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_project_id")
    private TargetProject targetProject;

    // 체크 당시의 응답 코드 (예: 200, 500)
    private int statusCode;

    // 체크된 시간
    private LocalDateTime checkedAt;
}