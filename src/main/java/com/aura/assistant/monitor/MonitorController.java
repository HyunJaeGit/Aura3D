package com.aura.assistant.monitor;

import com.aura.assistant.domain.MonitoringHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MonitorController 클래스
 * 모니터링 컨트롤러: 상태 코드와 Gemini AI 가이드를 함께 반환하도록 고도화되었습니다.
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitoringService monitoringService;

    /**
     * 모니터링 시작 메서드
     */
    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestParam("projectId") Long projectId) {
        monitoringService.startMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 시작되었습니다.");
    }

    /**
     * 모니터링 중지 메서드
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stop(@RequestParam("projectId") Long projectId) {
        monitoringService.stopMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 중지되었습니다.");
    }

    /**
     * 특정 프로젝트의 최신 데이터(상태 + AI가이드)를 가져오는 API
     * 리액트에서 최초 접속 시, 수동 클릭 시, 주기적 체크 시 호출됩니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLatestStatus(@RequestParam("projectId") Long projectId) {
        Map<String, Object> response = new HashMap<>();

        // [테스트 시뮬레이션] 1번 프로젝트면 강제로 500 에러와 가짜 가이드를 반환해봅니다.
        // 실제 운영 시에는 이 if문을 주석 처리하고 아래 서비스 호출 로직을 사용합니다.
        if (projectId == 1) {
            response.put("status", 500);
            response.put("aiGuide", "서버 응답 속도가 지연되고 있습니다. Gemini 분석 결과: 메모리 부족이 의심됩니다.");
            return ResponseEntity.ok(response);
        }

        // 실제 서비스 로직: DB에서 가장 최신 이력을 가져옴
        MonitoringHistory latest = monitoringService.getLatestHistory(projectId);

        if (latest != null) {
            response.put("status", latest.getStatusCode());
            response.put("aiGuide", latest.getAiGuide());
        } else {
            response.put("status", 200);
            response.put("aiGuide", null);
        }

        return ResponseEntity.ok(response);
    }
}