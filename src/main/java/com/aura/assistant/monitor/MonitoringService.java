package com.aura.assistant.monitor;

import com.aura.assistant.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * [클래스 역할]
 * 통합 관제 서비스: 기획안의 'On-Demand 모니터링'과 '장애 감지 & AI 분석'을 총괄합니다.
 * Java 21의 가상 스레드 스케줄러를 사용하여 매우 적은 리소스로 다수의 서버를 감시합니다.
 */
@Slf4j // 로그 기록 도구 (Lombok)
@Service // 스프링 서비스 빈 등록
@RequiredArgsConstructor // final 필드 자동 생성자 주입 (Lombok)
public class MonitoringService {

    private final TaskScheduler taskScheduler;
    private final TargetProjectRepository repository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;
    private final GeminiService geminiService; // 비서의 '뇌' 역할을 할 AI 서비스

    // 현재 실행 중인 모니터링 작업을 관리하는 지도 (프로젝트 ID : 작업 정보)
    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    /**
     * [메서드: startMonitoring]
     * 기획안 3번: 사용자가 버튼을 눌렀을 때 5분 주기로 스캐닝을 시작합니다.
     */
    public void startMonitoring(Long projectId) {
        TargetProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다. ID: " + projectId));

        if (activeTasks.containsKey(projectId)) {
            log.info("이미 모니터링이 가동 중입니다: {}", project.getName());
            return;
        }

        // 5분 주기로 서버 상태 체크 메서드(checkServerStatus)를 가상 스레드 상에서 실행
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> checkServerStatus(project),
                Duration.ofMinutes(5)
        );

        activeTasks.put(projectId, task);
        log.info("비서 가동 시작: [{}] 감시를 시작합니다. (5분 주기)", project.getName());
    }

    /**
     * [메서드: stopMonitoring]
     * 활성화된 스케줄링 작업을 중단하고 관리 목록에서 제거합니다.
     */
    public void stopMonitoring(Long projectId) {
        ScheduledFuture<?> task = activeTasks.get(projectId);
        if (task != null) {
            task.cancel(false); // 실행 중인 작업 취소
            activeTasks.remove(projectId); // 목록에서 삭제
            log.info("비서 가동 중지: 프로젝트 ID {}의 감시를 중단했습니다.", projectId);
        }
    }

    /**
     * [메서드: checkServerStatus]
     * 실제 서버 상태를 체크하고, 장애 시 AI 가이드를 받아 DB에 기록합니다.
     */
    @Transactional
    public void checkServerStatus(TargetProject project) {
        int responseCode;
        try {
            log.info("상태 체크 중: {}", project.getUrl());

            HttpURLConnection connection = (HttpURLConnection) URI.create(project.getUrl())
                    .toURL()
                    .openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5초 타임아웃

            responseCode = connection.getResponseCode();

        } catch (Exception e) {
            log.error("서버 체크 실패 (장애 감지): {}", e.getMessage());
            responseCode = 500; // 예외 발생 시 서버 에러 코드로 기록
        }

        // 1. TargetProject 엔티티의 현재 상태 정보 갱신
        project.updateStatus(responseCode);
        repository.save(project);

        // 2. 장애 발생 시(200 아님) Gemini AI로부터 조치 가이드 생성
        String aiGuide = null;
        if (responseCode != 200) {
            aiGuide = geminiService.getAiGuide(responseCode);
        }

        // 3. 기록 장부(MonitoringHistory)에 AI 조언을 포함하여 저장
        MonitoringHistory history = MonitoringHistory.builder()
                .targetProject(project)
                .statusCode(responseCode)
                .checkedAt(LocalDateTime.now())
                .aiGuide(aiGuide)
                .build();

        monitoringHistoryRepository.save(history);

        log.info("이력 저장 완료 - 프로젝트: {}, 상태: {}, AI가이드: {}",
                project.getName(), responseCode, aiGuide);
    }

    /**
     * [메서드: getLatestStatus]
     * 특정 프로젝트의 가장 최근 상태 코드를 DB에서 조회합니다.
     */
    public int getLatestStatus(Long projectId) {
        // findFirstBy...OrderByCheckedAtDesc: 가장 최근에 생성된 이력 1건을 가져오는 쿼리 메서드
        return monitoringHistoryRepository.findFirstByTargetProjectIdOrderByCheckedAtDesc(projectId)
                .map(MonitoringHistory::getStatusCode)
                .orElse(0); // 기록이 없으면 0 반환
    }

    // TO-BE: 이력 창고(MonitoringHistoryRepository)에서 최신 데이터를 가져옴
    public MonitoringHistory getLatestHistory(Long projectId) {
        return monitoringHistoryRepository.findFirstByTargetProjectIdOrderByCheckedAtDesc(projectId)
                .orElse(null);
    }

}