# Aura3D: Gemini AI 기반 3D 비주얼 관제 비서

**"텍스트 로그를 넘어, 3D 인터랙션과 AI 분석으로 서버 관제의 새로운 경험을 제공합니다."**

## Project Overview

Aura3D는 개발자가 서버 상태를 직관적으로 파악하고 장애에 즉각 대응할 수 있도록 돕는 **On-Demand형 관제 솔루션**입니다. 텍스트 중심의 기존 로그 시스템에서 벗어나, 3D 인터랙션과 AI 분석 리포트를 결합하여 운영 효율성을 극대화하는 것을 목표로 합니다.
특히 사용자가 시스템을 확인하는 시점에만 리소스를 활성화하는 **On-Demand 방식**을 채택하여 인프라 부하를 최적화하였습니다.

## Key Features

* **On-Demand Monitoring**: 사용자의 대시보드 접속 및 요청 시에만 모니터링 엔진 활성화
* **3D Visual Feedback**: Three.js 기반의 3D 캐릭터 애니메이션을 통한 서버 상태 표현 (정상: Idle / 장애: Alert)
* **AI Troubleshooting Guide**: Google Gemini 1.5 Flash 모델을 활용한 장애 원인 분석 및 실시간 조치 가이드 생성
* **High Performance Engine**: Java 21의 **Virtual Threads**를 도입하여 대규모 HTTP 체크 요청 시 리소스 점유 최소화

## Tech Stack

| 분류 | 기술 상세 |
| --- | --- |
| **Backend** | Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security, JWT |
| **AI Engine** | **Google Gemini 1.5 Flash API** |
| **Frontend** | React, **Three.js (React Three Fiber)** |
| **Database** | MySQL 8.0 |

---

## 💻 Core Logic

### 1. On-Demand Virtual Thread 제어

// MonitoringService.java
// 사용자 요청에 따라 가상 스레드 기반의 스케줄러를 동적으로 제어합니다.

```java
public void startMonitoring(Long projectId) {
    TargetProject project = repository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));
        
    if (activeTasks.containsKey(projectId)) return;

    // 가상 스레드(Virtual Threads) 기반의 TaskScheduler를 사용하여 비동기 작업을 수행
    ScheduledFuture task = taskScheduler.scheduleAtFixedRate(
        () -> checkServerStatus(project),
        Duration.ofMinutes(5)
    );
    activeTasks.put(projectId, task);
}

```

### 2. API 비용 최적화 로직

/**

* 상태 변화를 감지하여 '변했을 때만' AI 분석을 수행합니다.
* 이를 통해 불필요한 API 호출을 방지하고 운영 비용을 약 90% 절감했습니다.
*/

```java
@Transactional
public void checkServerStatus(TargetProject project) {
    // 1. DB에서 가장 최근에 저장했던 기록 조회
    MonitoringHistory lastHistory = monitoringHistoryRepository
        .findFirstByTargetProjectIdOrderByCheckedAtDesc(project.getId()).orElse(null);

    String aiGuide;
    // 2. [핵심] 상태 변화 감지: 최초 가동이거나 이전 상태와 지금이 다를 때만 AI 호출
    if (lastHistory == null || lastHistory.getStatusCode() != responseCode) {
        aiGuide = geminiService.getAiGuide(responseCode); // AI 호출 (비용 발생 지점)
    } else {
        aiGuide = lastHistory.getAiGuide(); // 이전 가이드를 재사용 (비용/성능 절약)
    }
}

```

---

## 주요 성과

* **비용 절감**: 상태 변화 감지 로직으로 상시 호출 대비 **AI API 소모량 약 90% 감소**
* **성능 최적화**: **Virtual Threads** 도입으로 대규모 요청 시에도 안정적인 메모리 유지
* **직관적 UX**: 텍스트 위주의 로그 시스템을 **3D 캐릭터 모션**으로 변환하여 인지 편의성 강화

