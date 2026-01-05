package com.aura.assistant.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini AI와 통신하여 서버 상태 분석 및 환영 메시지를 생성하는 서비스입니다.
 * 2026년 최신 모델(Gemini 2.5)을 사용하여 비용 최적화와 정확한 가이드를 제공합니다.
 */
@Service
public class GeminiService {

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    @Value("${google.ai.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 공통 API 호출 로직
     * 역할: 설정된 URL에 API Key를 조합하여 Google 서버에 분석을 요청합니다.
     */
    private String callGeminiApi(String prompt) {
        // [수정] URL 파라미터로 키를 전달하는 표준 방식을 유지합니다.
        String fullUrl = geminiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);
            return extractText(response);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            // [포트폴리오 포인트] 429 RESOURCE_EXHAUSTED 에러 대응 로직
            return "AI 분석 호출 한도 초과 (잠시 후 자동 재시도 예정)";
        } catch (Exception e) {
            // 404 에러 발생 시 로그에 상세 주소를 출력하여 디버깅을 돕습니다.
            return "AI 분석 일시 불가 (URL 확인 필요): " + e.getMessage();
        }
    }

    // 1. 단순 텍스트 생성 (테스트용)
    public String getCompletion(String prompt) {
        return callGeminiApi(prompt);
    }

    /**
     * 대시보드 진입 시 관리자에게 보여줄 환영 인사를 생성합니다.
     */
    public String getWelcomeGreeting(String userName) {
        String prompt = String.format(
                "관리자 %s님이 Aura3D 관제 센터에 접속했어. " +
                        "오늘의 날씨나 분위기를 고려해서 전문 비서처럼 아주 짧고 든든한 환영 인사를 한 줄로 해줘.", userName);
        return callGeminiApi(prompt);
    }

    /**
     * 서버의 HTTP 상태 코드를 분석하여 AI 조치 가이드를 생성합니다.
     */
    public String getAiGuide(int statusCode) {
        String prompt = (statusCode == 200)
                ? "서버 상태가 'Stable'이야. 관리자에게 시스템이 안정적으로 운영되고 있다는 짧은 브리핑을 해줘."
                : "긴급! 서버에서 " + statusCode + " 에러가 발생했어. 개발자가 즉시 확인해야 할 체크리스트를 한 줄로 요약해줘.";
        return callGeminiApi(prompt);
    }

    /**
     * Gemini의 복잡한 JSON 응답에서 실제 텍스트 내용만 추출합니다.
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.getFirst().get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.getFirst().get("text");
        } catch (Exception e) {
            return "AI 응답 해석 오류: 데이터 형식이 변경되었을 수 있습니다.";
        }
    }
}