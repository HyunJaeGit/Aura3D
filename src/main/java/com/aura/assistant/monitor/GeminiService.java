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

    public String getAiGuide(int statusCode) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // 상태에 따른 프롬프트 분기
        String prompt = (statusCode == 200)
                ? "현재 시스템은 매우 정상이야. 관리자에게 비서처럼 기분 좋은 인사와 시스템이 안정적이라는 브리핑을 한 줄로 해줘. 한국어로 40자 이내."
                : "서버 상태가 " + statusCode + " 에러야. 개발자가 할 조치사항을 전문 비서처럼 한 줄로 한국어로 알려줘. 50자 이내.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return extractText(response);
        } catch (Exception e) {
            return "AI 분석 일시 불가: " + e.getMessage();
        }
    }

    private String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) { return "가이드 생성 오류"; }
    }
}