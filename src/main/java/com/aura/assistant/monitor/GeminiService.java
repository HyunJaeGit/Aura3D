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
     * 공통 메서드
     * 역할: 외부(Service)에서 만든 프롬프트를 Gemini에게 전달하고 답변을 받습니다.
     * MonitoringService의 getAiAnalysis에서 이 메서드를 호출하게 됩니다.
     */
    public String getCompletion(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return extractText(response);
        } catch (Exception e) {
            return "AI 분석 일시 불가: " + e.getMessage();
        }
    }

     // 사용자가 대시보드에 처음 진입했을 때 보여줄 환영 인사를 생성합니다.
    public String getWelcomeGreeting(String userName) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // 프롬프트에 'userName'을 넣어 개인화된 느낌을 줍니다.
        String prompt = String.format(
                "관리자 %s님이 Aura3D 시스템 관제 센터에 로그인했어. " +
                        "전문적이고 든든한 AI 비서로서 짧고 정중한 환영 인사와 시스템 감시를 시작할 준비가 되었다는 메시지를 한 줄로 해줘. " +
                        "한국어로 40자 이내.", userName);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return extractText(response);
        } catch (Exception e) {
            return "접속을 환영합니다. 시스템 상태를 확인 중입니다.";
        }
    }


    public String getAiGuide(int statusCode) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // [기획 반영] 상태별 맞춤 프롬프트 설정
        String prompt = (statusCode == 200)
                ? "현재 시스템은 매우 정상이야. 관리자에게 비서처럼 기분 좋은 인사와 시스템이 안정적이라는 브리핑을 한 줄로 해줘. 한국어로 40자 이내."
                : "서버 상태가 " + statusCode + " 에러야. 개발자가 할 조치사항을 전문 비서처럼 한 줄로 한국어로 알려줘. 50자 이내.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            return extractText(response);
        } catch (Exception e) {
            return "AI 서비스 연결 일시 불가: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.getFirst().get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.getFirst().get("text");
        } catch (Exception e) {
            return "가이드 분석 중 오류 발생";
        }
    }
}