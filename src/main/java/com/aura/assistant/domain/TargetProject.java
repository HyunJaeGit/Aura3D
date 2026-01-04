package com.aura.assistant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * TargetProject 엔티티
 * 감시할 서버의 이름과 주소 정보를 저장하는 클래스입니다.
 */
@Entity
@Getter
@Setter // Lombok이 자동으로 유효한 Setter를 생성합니다.
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class TargetProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 프로젝트 별칭 (예: 나의 웹사이트)

    // [수정] unique = true를 추가하여 동일한 URL이 중복 저장되는 것을 DB 레벨에서 막습니다.
    @Column(nullable = false, unique = true)
    private String url;  // 감시할 주소 (예: https://www.google.com)

    private int lastStatus; // 마지막 HTTP 응답 코드

    // 상태 체크 시간을 기록하기 위해 추가하면 좋습니다.
    private LocalDateTime lastCheckTime;

    // 새로운 프로젝트를 등록할 때 사용하는 생성자
    public TargetProject(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // 상태 업데이트 메서드
    public void updateStatus(int status) {
        this.lastStatus = status;
        this.lastCheckTime = LocalDateTime.now();
    }

}