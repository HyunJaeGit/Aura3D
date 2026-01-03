package com.aura.assistant.domain.user;

import jakarta.persistence.*;
import lombok.*;

/**
 * [User 클래스]
 * 역할: 서비스 사용자 정보를 저장하는 엔티티입니다.
 * 프로젝트 도메인: com.aura.assistant.domain.user
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 아이디(이메일)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false)
    private String name; // 사용자 이름 (비서가 불러줄 이름)

    @Enumerated(EnumType.STRING)
    private Role role; // 권한 (USER, ADMIN)

}