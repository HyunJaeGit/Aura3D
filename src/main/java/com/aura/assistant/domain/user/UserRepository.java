package com.aura.assistant.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * [UserRepository 인터페이스]
 * 역할: DB의 'users' 테이블에 접근하여 데이터를 관리합니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일(로그인 ID)로 사용자 정보를 찾는 메서드입니다.
     * Optional을 사용하여 사용자가 없을 경우의 예외 처리를 안전하게 돕습니다.
     */
    Optional<User> findByEmail(String email);
}