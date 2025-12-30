package com.aura.assistant.monitor;

import com.aura.assistant.domain.MonitoringHistory;
import com.aura.assistant.domain.MonitoringHistoryRepository;
import com.aura.assistant.domain.TargetProject;
import com.aura.assistant.domain.TargetProjectRepository;
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
 * MonitoringService 클래스
 * 프로젝트의 상태를 주기적으로 점검하고 결과를 DB(TargetProject, MonitoringHistory)에 기록합니다.
 * Java 21 가상 스레드 스케줄러를 활용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final TaskScheduler taskScheduler;
    private final TargetProjectRepository repository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;

    // 현재 실행 중인 모니터링 작업을 관리하는 Map (프로젝트 ID : 작업 정보)
    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    /**
     * 모니터링 시작
     * 5분 주기로 해당 프로젝트의 상태를 체크하는 작업을 등록합니다.
     */
    public void startMonitoring(Long projectId) {
        TargetProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다. ID: " + projectId));

        if (activeTasks.containsKey(projectId)) {
            log.info("이미 모니터링이 가동 중입니다: {}", project.getName());
            return;
        }

        // 5분 주기로 실행되는 스케줄 등록
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> checkServerStatus(project),
                Duration.ofMinutes(5)
        );

        activeTasks.put(projectId, task);
        log.info("비서 가동 시작: [{}] 감시를 시작합니다. (5분 주기)", project.getName());
    }

    /**
     * 모니터링 중지
     */
    public void stopMonitoring(Long projectId) {
        ScheduledFuture<?> task = activeTasks.get(projectId);
        if (task != null) {
            task.cancel(false);
            activeTasks.remove(projectId);
            log.info("비서 가동 중지: 프로젝트 ID {}의 감시를 중단했습니다.", projectId);
        }
    }

    /**
     * 실제 서버 상태 체크 및 데이터 저장 로직
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
            connection.setConnectTimeout(5000);

            responseCode = connection.getResponseCode();

        } catch (Exception e) {
            log.error("서버 체크 실패 (장애 감지): {}", e.getMessage());
            responseCode = 500; // 에러 발생 시 500으로 기록
        }

        // 1. TargetProject의 현재 상태 업데이트 및 저장
        project.updateStatus(responseCode);
        repository.save(project);

        // 2. MonitoringHistory에 새로운 이력 추가
        MonitoringHistory history = MonitoringHistory.builder()
                .targetProject(project)
                .statusCode(responseCode)
                .checkedAt(LocalDateTime.now())
                .build();

        monitoringHistoryRepository.save(history);

        log.info("이력 저장 완료 - 프로젝트: {}, 상태코드: {}", project.getName(), responseCode);
    }
}