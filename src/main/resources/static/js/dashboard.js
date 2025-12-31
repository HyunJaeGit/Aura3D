/**
 * dashboard.js
 * 모니터링 대시보드의 실시간 통신을 담당하는 스크립트입니다.
 */

// 모든 프로젝트의 상태를 일괄 업데이트하는 함수
function updateAllStatuses() {
    const statusElements = document.querySelectorAll('[id^="status-"]');

    statusElements.forEach(el => {
        const projectId = el.id.split('-')[1];

        fetch('/api/monitoring/status?projectId=' + projectId)
            .then(response => response.json())
            .then(status => {
                el.innerText = status;
                el.className = (status === 200) ? 'status-up' : 'status-down';

                // ==========================================
                // [수정 포인트] 3D 캐릭터에게 상태 전달
                // 전역 변수 window.currentAuraStatus에 상태를 저장하여
                // 나중에 구현할 React 3D 컴포넌트가 이 값을 감시하게 합니다.
                // ==========================================
                window.currentAuraStatus = status;
                // ==========================================
            })
            .catch(error => console.error('상태 확인 실패(ID:' + projectId + '):', error));
    });
}

// 모니터링 시작 요청
function startMonitoring(projectId) {
    fetch('/api/monitoring/start?projectId=' + projectId, { method: 'POST' })
        .then(response => {
            if (response.ok) {
                alert('비서가 감시를 시작했습니다!');
                updateAllStatuses(); // 즉시 갱신
            } else {
                alert('가동 실패! 로그를 확인하세요.');
            }
        });
}

// 모니터링 중지 요청
function stopMonitoring(projectId) {
    fetch('/api/monitoring/stop?projectId=' + projectId, { method: 'POST' })
        .then(response => {
            if (response.ok) {
                alert('비서가 휴식에 들어갔습니다.');
                location.reload();
            }
        });
}

// 페이지 로드 시 실행 설정
window.onload = function() {
    updateAllStatuses(); // 첫 실행
    setInterval(updateAllStatuses, 30000); // 30초마다 반복
};

/**
 * 새로운 프로젝트를 등록합니다.
 */
function addProject() {
    const name = document.getElementById('newName').value;
    const url = document.getElementById('newUrl').value;

    if (!name || !url) {
        alert("이름과 URL을 모두 입력해주세요.");
        return;
    }

    // FormData를 사용하여 데이터를 전송합니다.
    const formData = new URLSearchParams();
    formData.append('name', name);
    formData.append('url', url);

    fetch('/api/projects', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            alert('새 프로젝트가 등록되었습니다!');
            location.reload(); // 리스트 갱신을 위해 새로고침
        }
    })
    .catch(error => console.error('등록 실패:', error));
}