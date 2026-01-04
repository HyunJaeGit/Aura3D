package com.aura.assistant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TargetProjectRepository
 * 데이터베이스의 target_project 테이블에 접근하는 도구입니다.
 */
@Repository
public interface TargetProjectRepository extends JpaRepository<TargetProject, Long> {

    /**
     * URL 중복 여부를 확인하는 메서드
     * 이 메서드 이름만 선언하면 Spring Data JPA가 자동으로 "SELECT count(*) > 0 FROM target_project WHERE url = ?" 쿼리를 실행
     */
    boolean existsByUrl(String url);
}