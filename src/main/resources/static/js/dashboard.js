/**
 * [Aura3D Dashboard Script]
 * 역할: 순수하게 프로젝트 상태 점검 및 버튼 이벤트만 담당
 */

function updateAllStatuses() {
    const statusElements = document.querySelectorAll('[id^="status-"]');
    statusElements.forEach(el => {
        const projectId = el.id.split('-')[1];
        fetch('/api/monitoring/status?projectId=' + projectId)
            .then(res => res.json())
            .then(status => {
                el.innerText = status;
                el.className = (status === 200) ? 'status-up' : 'status-down';
            })
            .catch(err => console.error('상태 확인 실패:', err));
    });
}

window.startMonitoring = function(projectId) {
    fetch('/api/monitoring/start?projectId=' + projectId, { method: 'POST' })
        .then(res => { if(res.ok) { alert("시작!"); updateAllStatuses(); } });
}

window.stopMonitoring = function(projectId) {
    fetch('/api/monitoring/stop?projectId=' + projectId, { method: 'POST' })
        .then(res => { if(res.ok) { alert("중지!"); updateAllStatuses(); } });
}

window.addEventListener('load', () => {
    updateAllStatuses();
    setInterval(updateAllStatuses, 30000);
});