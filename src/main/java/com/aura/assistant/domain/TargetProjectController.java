package com.aura.assistant.domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * [TargetProjectController]
 * 역할: 모니터링 대상 프로젝트(URL)의 등록, 조회, 삭제를 담당하는 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173") // 리액트 포트 허용
public class TargetProjectController {

    private final TargetProjectRepository repository;

    // 생성자 주입 방식 (의존성 주입)
    public TargetProjectController(TargetProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * [1. 프로젝트 등록]
     * 사용자가 입력한 이름과 URL을 받아 중복 체크 후 DB에 저장합니다.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProject(@RequestBody TargetProject project) {

        // [중복 체크] Repository에 추가한 existsByUrl 메서드를 활용합니다.
        if (repository.existsByUrl(project.getUrl())) {
            // 이미 존재하면 400 Bad Request 에러와 메시지를 보냅니다.
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "이미 등록된 모니터링 주소입니다."));
        }

        // 새 프로젝트 저장
        TargetProject savedProject = repository.save(project);

        // 성공 시 저장된 객체 반환
        return ResponseEntity.ok(savedProject);
    }

    /**
     * [2. 프로젝트 목록 조회]
     * 대시보드 화면에 뿌려줄 전체 모니터링 리스트를 가져옵니다.
     */
    @GetMapping("/list")
    public List<TargetProject> getAllProjects() {
        return repository.findAll();
    }

    /**
     * [3. 프로젝트 삭제]
     * 모니터링을 중단하고 싶은 프로젝트를 삭제합니다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}