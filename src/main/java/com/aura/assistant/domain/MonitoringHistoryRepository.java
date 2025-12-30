package com.aura.assistant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * MonitoringHistoryRepository 인터페이스
 * 모니터링 이력 데이터를 DB에서 다루기 위한 저장소입니다.
 */
public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, Long> {

    // 프로젝트 ID로 찾아서, 체크 시간(checkedAt) 기준 내림차순 정렬 후 첫 번째 데이터만 가져오기
    Optional<MonitoringHistory> findFirstByTargetProjectIdOrderByCheckedAtDesc(Long projectId);
}