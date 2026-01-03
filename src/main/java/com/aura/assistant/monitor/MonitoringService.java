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
 * [MonitoringService]
 * 역할: 등록된 서버의 상태를 주기적으로 체크하고, AI 비서(Gemini)와 연동하여 분석 가이드를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final TaskScheduler taskScheduler;
    private final TargetProjectRepository repository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;
    private final GeminiService geminiService;

    // 현재 감시 중인 작업들을 메모리에 저장 (중복 실행 방지 및 중지용)
    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    /**
     * 모니터링 시작: 설정된 시간(5분)마다 반복해서 서버 상태를 체크합니다.
     */
    public void startMonitoring(Long projectId) {
        TargetProject project = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다. ID: " + projectId));

        if (activeTasks.containsKey(projectId)) return;

        // 5분마다 checkServerStatus 메서드를 실행하도록 예약
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> checkServerStatus(project),
                Duration.ofMinutes(5)
        );

        activeTasks.put(projectId, task);
        log.info("비서 가동 시작: [{}] 감시 시작.", project.getName());
    }

    /**
     * 모니터링 중지: 진행 중인 예약 작업을 취소합니다.
     */
    public void stopMonitoring(Long projectId) {
        ScheduledFuture<?> task = activeTasks.get(projectId);
        if (task != null) {
            task.cancel(false);
            activeTasks.remove(projectId);
            log.info("비서 가동 중지: 프로젝트 ID {}", projectId);
        }
    }

    /**
     * [핵심 로직] 서버 상태 체크 및 Gemini AI 연동
     * 1. 실제 URL에 접속하여 상태 코드(200, 500 등)를 가져옵니다.
     * 2. 이전 상태와 비교하여 '상태가 변했을 때만' AI 답변을 새롭게 생성합니다.
     */
    @Transactional
    public void checkServerStatus(TargetProject project) {
        int responseCode;
        try {
            // [수정 포인트] URI를 통해 URL 연결 객체를 생성합니다.
            HttpURLConnection connection = (HttpURLConnection) URI.create(project.getUrl()).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5초 안에 응답 없으면 에러 처리

            // [추가 포인트] 중요! 실제 사이트(네이버 등) 차단 방지를 위한 '브라우저 신분증' 설정
            // 이 설정이 없으면 네이버는 봇으로 인식하여 연결을 끊어버릴 수 있습니다(500 에러 유발).
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            responseCode = connection.getResponseCode();

            // 참고: 네이버가 보안 페이지로 이동(302)시키는 경우도 정상(200)으로 간주합니다.
            if (responseCode == 302 || responseCode == 301) responseCode = 200;

        } catch (Exception e) {
            // 연결 중 오류 발생 시 500(장애)으로 처리
            log.error("연결 실패 (장애 감지): {}", e.getMessage());
            responseCode = 500;
        }

        // 1. DB에서 가장 최근에 저장했던 모니터링 기록을 하나 가져옵니다.
        MonitoringHistory lastHistory = monitoringHistoryRepository
                .findFirstByTargetProjectIdOrderByCheckedAtDesc(project.getId()).orElse(null);

        String aiGuide;

        // 2. [기획 핵심] 상태 변화 감지 로직
        // - 처음 체크하거나, 이전 상태와 지금 상태가 다를 때만 Gemini AI에게 물어봅니다.
        if (lastHistory == null || lastHistory.getStatusCode() != responseCode) {
            log.info("상태 변화 감지: {} -> {}. Gemini AI 분석 요청...",
                    (lastHistory != null ? lastHistory.getStatusCode() : "최초 가동"), responseCode);

            // GeminiService를 호출하여 상태에 맞는 응답 메시지를 생성합니다.
            aiGuide = geminiService.getAiGuide(responseCode);
        } else {
            // 상태가 변하지 않았다면 AI를 호출하지 않고, 이전 말풍선 메시지를 그대로 사용합니다. (비용/성능 절약)
            aiGuide = lastHistory.getAiGuide();
        }

        // 3. 프로젝트의 최신 상태 정보를 업데이트하고 기록을 저장합니다.
        project.updateStatus(responseCode);
        repository.save(project);

        MonitoringHistory history = MonitoringHistory.builder()
                .targetProject(project)
                .statusCode(responseCode)
                .checkedAt(LocalDateTime.now())
                .aiGuide(aiGuide)
                .build();

        monitoringHistoryRepository.save(history);
        log.info("모니터링 기록 저장 완료: 상태코드 {}", responseCode);
    }

    /**
     * 리액트 화면에 보여줄 최신 상태 정보를 조회합니다.
     */
    public MonitoringHistory getLatestHistory(Long projectId) {
        return monitoringHistoryRepository.findFirstByTargetProjectIdOrderByCheckedAtDesc(projectId)
                .orElse(null);
    }
}