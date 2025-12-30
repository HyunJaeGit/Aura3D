package com.aura.assistant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * MonitoringHistoryRepository 인터페이스
 * MonitoringHistory 엔티티를 DB에 저장하고 관리하는 레포지토리입니다.
 */
@Repository
public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, Long> {
}