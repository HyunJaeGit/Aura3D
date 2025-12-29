package com.aura.assistant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TargetProjectRepository
 * 데이터베이스의 target_project 테이블에 접근하는 도구입니다.
 */
@Repository
public interface TargetProjectRepository extends JpaRepository<TargetProject, Long> {
    // 기본적으로 저장(save), 조회(findById) 기능을 제공합니다.
}