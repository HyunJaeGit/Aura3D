package com.aura.assistant.controller;

import com.aura.assistant.domain.TargetProject;
import com.aura.assistant.domain.TargetProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * WebController 클래스
 * 사용자에게 보여줄 HTML 화면(View)을 제어합니다.
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final TargetProjectRepository targetProjectRepository;

    /**
     * 메인 대시보드 화면 호출
     * DB에 저장된 프로젝트 목록을 가지고 dashboard.html 화면으로 이동합니다.
     */
    @GetMapping("/")
    public String dashboard(Model model) {
        // DB에서 모든 감시 대상 프로젝트를 가져와 화면에 전달합니다.
        model.addAttribute("projects", targetProjectRepository.findAll());

        // src/main/resources/templates/dashboard.html 파일을 찾아서 보여줍니다.
        return "dashboard";
    }

    /**
     * 프로젝트 등록 API
     * @param name 프로젝트 이름
     * @param url 감시할 URL
     */
    @PostMapping("/api/projects")
    @ResponseBody
    public ResponseEntity<String> addProject(@RequestParam("name") String name, @RequestParam("url") String url) {
        TargetProject project = new TargetProject();
        project.setName(name);
        project.setUrl(url);
        project.setLastStatus(0); // 초기 상태는 0

        targetProjectRepository.save(project);
        return ResponseEntity.ok("등록 성공");
    }

}