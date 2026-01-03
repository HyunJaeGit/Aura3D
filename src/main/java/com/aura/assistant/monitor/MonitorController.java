package com.aura.assistant.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MonitorController 클래스
 * 화면으로부터 모니터링 시작/중지 요청을 받는 입구 역할을 합니다.
 */
@RestController
@RequestMapping("/api/monitoring") // HTML fetch 주소와 일치하도록 수정 (monitor -> monitoring)
@RequiredArgsConstructor
public class MonitorController {

    private final MonitoringService monitoringService;

    /**
     * 모니터링 시작 메서드
     * @param projectId 화면에서 전달받은 프로젝트의 고유 번호
     * @return 성공 메시지
     */
    @PostMapping("/start") // HTML의 /api/monitoring/start 와 맞춤
    public ResponseEntity<String> start(@RequestParam("projectId") Long projectId) {
        // 쿼리 파라미터(?projectId=1) 방식으로 데이터를 받습니다.
        monitoringService.startMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 시작되었습니다.");
    }

    /**
     * 모니터링 중지 메서드
     * @param projectId 화면에서 전달받은 프로젝트의 고유 번호
     * @return 성공 메시지
     */
    @PostMapping("/stop") // HTML의 /api/monitoring/stop 과 맞춤
    public ResponseEntity<String> stop(@RequestParam("projectId") Long projectId) {
        monitoringService.stopMonitoring(projectId);
        return ResponseEntity.ok("모니터링이 중지되었습니다.");
    }

    /**
     * 특정 프로젝트의 최신 상태 코드를 가져오는 API
     * 주소: GET http://localhost:8080/api/monitoring/status?projectId=1
     * * @param projectId 상태를 확인할 프로젝트 ID
     * @return 최신 상태 코드 (예: 200, 404 등)
     */
    /*
    @GetMapping("/status")
    public ResponseEntity<Integer> getLatestStatus(@RequestParam("projectId") Long projectId) {
        // 서비스에게 최신 상태 코드가 무엇인지 물어봅니다.
        int latestStatus = monitoringService.getLatestStatus(projectId);

        // 찾은 상태 코드를 화면에 전달합니다.
        return ResponseEntity.ok(latestStatus);
    }
     */
    // 500 에러 테스트용 코드
    @GetMapping("/status")
    public ResponseEntity<Integer> getLatestStatus(@RequestParam("projectId") Long projectId) {
        // 테스트용: 1번 프로젝트면 무조건 장애(500) 발생 시뮬레이션
        if (projectId == 1) {
            return ResponseEntity.ok(500);
        }
        return ResponseEntity.ok(monitoringService.getLatestStatus(projectId));
    }


}