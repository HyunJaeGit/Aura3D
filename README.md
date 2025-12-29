# Aura3D  
Gemini AI 기반 3D 비주얼 관제 비서

## Project Overview
Aura3D는 개발자가 서버 상태를 직관적으로 파악하고 장애에 즉각 대응할 수 있도록 돕는 On-Demand형 관제 솔루션입니다. 텍스트 중심의 기존 로그 시스템에서 벗어나, 3D 인터랙션과 AI 분석 리포트를 결합하여 운영 효율성을 극대화하는 것을 목표로 합니다.

특히 사용자가 시스템을 확인하는 시점에만 리소스를 활성화하는 On-Demand 방식을 채택하여 클라우드 인프라 비용을 최적화하였습니다.

---

## Key Features
1. On-Demand Monitoring
   - 사용자의 대시보드 접속 및 요청 시에만 모니터링 엔진 활성화
   - 자원 낭비를 방지하고 필요한 시점에 집중적인 상태 스캐닝 수행 (5분 주기)

2. 3D Visual Feedback
   - Three.js 기반의 3D 캐릭터 애니메이션을 통한 서버 상태 표현
   - 정상(Idle), 장애(Alert) 상태를 시각적으로 즉각 구분 가능

3. AI Troubleshooting Guide
   - Google Gemini 1.5 Flash 모델을 활용한 장애 원인 분석
   - 상태 코드 및 응답 메시지에 기반한 실시간 조치 가이드 생성

4. High Performance Engine
   - Java 21의 Virtual Threads를 도입하여 대규모 HTTP 체크 요청 처리 시 리소스 점유 최소화

---

## Tech Stack
- Backend: Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security, JWT
- AI: Google Gemini 1.5 Flash API
- Frontend: React, Three.js (React Three Fiber)
- Database: MySQL 8.0
- DevOps: Docker, Docker Compose, GitHub Actions

---

## Roadmap (5-Day Sprint)
- Day 1: 백엔드 엔진 구축 및 Virtual Thread 스케줄러 구현
- Day 2: Gemini AI 연동 및 보안(JWT) 체계 수립
- Day 3: 3D 캐릭터 모델링 및 상태별 애니메이션 리깅
- Day 4: React 대시보드 구축 및 실시간 데이터 동기화
- Day 5: 컨테이너화(Docker) 및 배포 자동화 시나리오 검증

---

## System Flow
1. User Request: 사용자가 대시보드에서 모니터링 활성화 신호 전송
2. Scheduling: 가상 스레드 기반 스케줄러가 타겟 URL의 상태 확인
3. AI Analysis: HTTP 장애 발생 시 Gemini API를 통한 분석 수행 및 DB 기록
4. Visualization: 상태 데이터를 프론트엔드로 전송하여 3D 캐릭터 모션 변경
