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

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final TaskScheduler taskScheduler;
    private final TargetProjectRepository repository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;
    private final GeminiService geminiService;

    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    public void startMonitoring(Long projectId) {
        TargetProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다. ID: " + projectId));

        if (activeTasks.containsKey(projectId)) return;

        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> checkServerStatus(project),
                Duration.ofMinutes(5)
        );

        activeTasks.put(projectId, task);
        log.info("비서 가동 시작: [{}] 감시 시작.", project.getName());
    }

    public void stopMonitoring(Long projectId) {
        ScheduledFuture<?> task = activeTasks.get(projectId);
        if (task != null) {
            task.cancel(false);
            activeTasks.remove(projectId);
        }
    }

    /**
     * [핵심 수정] 서버 상태를 체크하고, 상태가 '변했을 때만' Gemini AI 답변을 생성합니다.
     */
    @Transactional
    public void checkServerStatus(TargetProject project) {
        int responseCode;
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(project.getUrl()).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            responseCode = connection.getResponseCode();
        } catch (Exception e) {
            responseCode = 500;
        }

        // 1. 최신 기록 조회
        MonitoringHistory lastHistory = monitoringHistoryRepository
                .findFirstByTargetProjectIdOrderByCheckedAtDesc(project.getId()).orElse(null);

        String aiGuide;

        // 2. [기획 반영] 상태 변화 시에만 Gemini 호출
        if (lastHistory == null || lastHistory.getStatusCode() != responseCode) {
            log.info("상태 변화 감지: {} -> {}. AI 브리핑 생성 중...",
                    (lastHistory != null ? lastHistory.getStatusCode() : "최초"), responseCode);
            aiGuide = geminiService.getAiGuide(responseCode);
        } else {
            // 상태 유지 시 이전 가이드 재사용
            aiGuide = lastHistory.getAiGuide();
        }

        // 3. 기록 저장
        project.updateStatus(responseCode);
        repository.save(project);

        MonitoringHistory history = MonitoringHistory.builder()
                .targetProject(project)
                .statusCode(responseCode)
                .checkedAt(LocalDateTime.now())
                .aiGuide(aiGuide)
                .build();

        monitoringHistoryRepository.save(history);
    }

    public MonitoringHistory getLatestHistory(Long projectId) {
        return monitoringHistoryRepository.findFirstByTargetProjectIdOrderByCheckedAtDesc(projectId)
                .orElse(null);
    }

}