package com.aura.assistant.monitor;

import com.aura.assistant.domain.TargetProject;
import com.aura.assistant.domain.TargetProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI; // URL 대신 URI를 사용하기 위해 추가
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * MonitoringService 클래스
 * 사용자의 요청에 따라 모니터링을 동적으로 시작하거나 중지합니다.
 * Java 21 가상 스레드와 최신 네트워크 API를 사용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final TaskScheduler taskScheduler;
    private final TargetProjectRepository repository;

    // 현재 실행 중인 모니터링 작업을 관리하는 Map (프로젝트 ID : 작업 정보)
    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    /**
     * 모니터링 시작 (On-Demand)
     * 사용자가 버튼을 눌렀을 때 호출되어 5분 주기 감시를 시작합니다.
     */
    public void startMonitoring(Long projectId) {
        TargetProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다. ID: " + projectId));

        if (activeTasks.containsKey(projectId)) {
            log.info("이미 모니터링이 가동 중입니다: {}", project.getName());
            return;
        }

        // 5분 주기로 실행되는 가상 스레드 스케줄 등록 (람다식 최적화 적용)
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> checkServerStatus(project),
                Duration.ofMinutes(5)
        );

        activeTasks.put(projectId, task);
        log.info("비서 가동 시작: [{}] 감시를 시작합니다. (5분 주기)", project.getName());
    }

    /**
     * 모니터링 중지
     * 사용자가 대시보드를 이탈하거나 중지 버튼을 누르면 호출됩니다.
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
     * 실제 서버 상태 체크 로직 (가상 스레드 내에서 실행됨)
     */
    private void checkServerStatus(TargetProject project) {
        try {
            log.info("상태 체크 중: {}", project.getUrl());

            // [수정] Java 20 이상에서 권장되는 URI -> URL 변환 방식입니다.
            HttpURLConnection connection = (HttpURLConnection) URI.create(project.getUrl())
                    .toURL()
                    .openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5초 타임아웃

            int responseCode = connection.getResponseCode();

            // 결과 업데이트 (Dirty Checking이 아닌 명시적 save 호출)
            project.updateStatus(responseCode);
            repository.save(project);

            log.info("체크 결과 - 프로젝트: {}, 상태코드: {}", project.getName(), responseCode);

        } catch (Exception e) {
            log.error("서버 체크 실패 (장애 감지): {}", e.getMessage());
            project.updateStatus(500); // 통신 실패 시 500(Internal Server Error)으로 간주
            repository.save(project);
        }
    }
}