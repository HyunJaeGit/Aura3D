package com.aura.assistant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TargetProject 엔티티
 * 감시할 서버의 이름과 주소 정보를 저장하는 클래스입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class TargetProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 프로젝트 별칭 (예: 나의 웹사이트)

    @Column(nullable = false)
    private String url;  // 감시할 주소 (예: https://www.google.com)

    private int lastStatus; // 마지막 HTTP 응답 코드

    // 새로운 프로젝트를 등록할 때 사용하는 생성자입니다.
    public TargetProject(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // 서버의 상태 코드를 업데이트하는 메서드입니다.
    public void updateStatus(int status) {
        this.lastStatus = status;
    }
}