package com.aura.assistant.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

/**
 * GeminiService
 * 기획안 Day 2: 장애 발생 시 Gemini API를 호출하여 조치 가이드를 생성합니다.
 * 역할: Java 21의 기능을 활용해 가볍고 안전하게 AI 응답을 처리합니다.
 */
@Service
public class GeminiService {

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 상태 코드를 바탕으로 Gemini에게 조언을 구합니다.
     * (Warning: 'never used'는 나중에 MonitoringService에서 호출하면 사라집니다.)
     */
    public String getAiGuide(int statusCode) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", "서버 상태가 " + statusCode + "입니다. 개발자가 할 조치사항을 한 줄로 한국어로 알려줘. 50자 이내.")
                        ))
                )
        );

        try {
            // Unchecked assignment 경고 해결을 위해 구체적인 타입 명시
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return extractText(response);
        } catch (Exception e) {
            return "AI 분석 일시 불가: " + e.getMessage();
        }
    }

    /**
     * Java 21의 getFirst()를 사용하여 가독성을 높이고 경고를 해결합니다.
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        try {
            if (response == null || !response.containsKey("candidates")) return "응답 없음";

            // Java 21 최신 문법: .get(0) 대신 .getFirst() 사용
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.getFirst().get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.getFirst().get("text");
        } catch (Exception e) {
            return "조치 가이드 생성 중 구조 분석 오류";
        }
    }
}