package com.aura.assistant.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitoringService monitoringService;

    @PostMapping("/start/{id}")
    public String start(@PathVariable Long id) {
        monitoringService.startMonitoring(id);
        return "모니터링이 시작되었습니다.";
    }

    @PostMapping("/stop/{id}")
    public String stop(@PathVariable Long id) {
        monitoringService.stopMonitoring(id);
        return "모니터링이 중지되었습니다.";
    }
}