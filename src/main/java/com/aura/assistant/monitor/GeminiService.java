package com.aura.assistant.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    // [추가] 설정 파일에서 URL을 가져옵니다.
    @Value("${google.ai.gemini.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 공통 API 호출 로직
     * 역할: 주소를 조립하고 Gemini에게 요청을 보냅니다.
     */
    private String callGeminiApi(String prompt) {
        // 설정 파일의 URL 뒤에 API 키만 붙여서 완성합니다.
        String fullUrl = geminiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(fullUrl, requestBody, Map.class);
            return extractText(response);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            return "AI 사용량이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
        } catch (Exception e) {
            return "AI 분석 일시 불가: " + e.getMessage();
        }
    }

    // 1. 공통 메서드 (기존 유지하되 내부 로직 개선)
    public String getCompletion(String prompt) {
        return callGeminiApi(prompt);
    }

    // 2. 환영 인사 생성 (모델 주소를 2.0으로 자동 적용)
    public String getWelcomeGreeting(String userName) {
        String prompt = String.format(
                "관리자 %s님이 Aura3D 시스템 관제 센터에 로그인했어. " +
                        "전문적이고 든든한 AI 비서로서 짧고 정중한 환영 인사와 시스템 감시를 시작할 준비가 되었다는 메시지를 한 줄로 해줘. " +
                        "한국어로 40자 이내.", userName);
        return callGeminiApi(prompt);
    }

    // 3. AI 가이드 생성 (모델 주소를 2.0으로 자동 적용)
    public String getAiGuide(int statusCode) {
        String prompt = (statusCode == 200)
                ? "현재 시스템은 매우 정상이야. 관리자에게 비서처럼 기분 좋은 인사와 시스템이 안정적이라는 브리핑을 한 줄로 해줘. 한국어로 40자 이내."
                : "서버 상태가 " + statusCode + " 에러야. 개발자가 할 조치사항을 전문 비서처럼 한 줄로 한국어로 알려줘. 50자 이내.";
        return callGeminiApi(prompt);
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