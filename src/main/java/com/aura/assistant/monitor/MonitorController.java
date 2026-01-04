package com.aura.assistant.monitor;

import com.aura.assistant.domain.MonitoringHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * [MonitorController]
 * 역할: 리액트 프론트엔드와 소통하는 창구입니다.
 * 리액트에서 "지금 상태 어때?"라고 물어보면 DB에서 최신 정보를 꺼내 대답해줍니다.
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitoringService monitoringService;
    private final GeminiService geminiService; // GeminiService 주입 추가

    /**
     * [추가] 대시보드 첫 진입 시 AI 환영 인사를 가져옵니다.
     */
    @GetMapping("/welcome")
    public ResponseEntity<String> getWelcome(@RequestParam("userName") String userName) {
        // GeminiService를 직접 호출하여 실시간 인사말을 생성합니다.
        String greeting = geminiService.getWelcomeGreeting(userName);
        return ResponseEntity.ok(greeting);
    }

    /**
     * 모니터링 시작: 특정 프로젝트의 감시 스케줄러를 가동합니다.
     */
    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestParam("projectId") Long projectId) {
        monitoringService.startMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 시작되었습니다.");
    }

    /**
     * 모니터링 중지: 진행 중인 감시 작업을 멈춥니다.
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stop(@RequestParam("projectId") Long projectId) {
        monitoringService.stopMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 중지되었습니다.");
    }

    /**
     * 특정 프로젝트의 최신 데이터(DB 값)를 반환합니다.
     * 이제 더 이상 가짜 500 에러를 보내지 않고, 실제 모니터링 결과를 보냅니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLatestStatus(@RequestParam("projectId") Long projectId) {
        Map<String, Object> response = new HashMap<>();

        // 1. 서비스 클래스를 통해 DB에서 해당 프로젝트의 가장 최근 기록을 가져옵니다.
        MonitoringHistory latest = monitoringService.getLatestHistory(projectId);

        // 2. 기록이 존재한다면 실제 상태코드와 AI 가이드를 응답에 담습니다.
        if (latest != null) {
            response.put("status", latest.getStatusCode());
            response.put("aiGuide", latest.getAiGuide());
        } else {
            // 아직 한 번도 체크된 적이 없다면 기본 정상 상태를 반환합니다.
            response.put("status", 200);
            response.put("aiGuide", "데이터를 수집 중입니다. 잠시만 기다려주세요.");
        }

        return ResponseEntity.ok(response);
    }
}