/**
 * [Aura3D Dashboard Script - Mesh Test Version]
 * 초보자 가이드: 이 코드는 외부 파일을 불러오지 않고 브라우저가 직접 3D 상자를 그리게 합니다.
 */

// dashboard.js 상단
const React = window.React;
const ReactDOM = window.ReactDOM;

// 브라우저마다 등록되는 이름이 다를 수 있어 3가지를 모두 체크합니다.
const RTF = window.ReactThreeFiber || window.ThreeFiber || (window.ReactThree && window.ReactThree.Fiber);

// ==========================================
// [1] 프로젝트 관리 로직 (Vanilla JS - 기존과 동일)
// ==========================================
function updateAllStatuses() {
    const statusElements = document.querySelectorAll('[id^="status-"]');
    statusElements.forEach(el => {
        const projectId = el.id.split('-')[1];
        fetch('/api/monitoring/status?projectId=' + projectId)
            .then(res => res.json())
            .then(status => {
                el.innerText = status;
                el.className = (status === 200) ? 'status-up' : 'status-down';
                window.currentAuraStatus = status;
            })
            .catch(err => console.error('상태 확인 실패:', err));
    });
}

// ... (startMonitoring, stopMonitoring, addProject 함수는 이전과 동일하게 유지)

// ==========================================
// [2] 3D 메쉬 렌더링 로직 (순수 자바스크립트 방식)
// ==========================================

/**
 * 테스트용 주황색 상자 컴포넌트
 */
function TestBox() {
    // mesh를 생성하고 그 안에 기하구조(Box)와 재질(StandardMaterial)을 넣습니다.
    return React.createElement('mesh', { rotation: [0.5, 0.5, 0] }, [
        React.createElement('boxGeometry', { args: [2, 2, 2], key: 'geo' }),
        React.createElement('meshStandardMaterial', { color: 'orange', key: 'mat' })
    ]);
}

/**
 * 3D 캔버스 뷰
 */
function DashboardView() {
    // 라이브러리 로드 실패 시 화면에 메시지 표시
    if (!RTF) {
        return React.createElement('div', { style: { color: 'white', padding: '20px' } },
            '라이브러리(Fiber)를 찾을 수 없습니다. HTML 로드 순서를 확인하세요.');
    }

    // <Canvas> <ambientLight /> <TestBox /> </Canvas> 구조를 생성
    return React.createElement(RTF.Canvas, {
        camera: { position: [0, 0, 5] },
        style: { background: '#1a1a1a', height: '100%', borderRadius: '15px' }
    }, [
        React.createElement('ambientLight', { intensity: 0.5, key: 'light1' }),
        React.createElement('pointLight', { position: [10, 10, 10], key: 'light2' }),
        React.createElement(TestBox, { key: 'box' })
    ]);
}

// 페이지 로드 시 실행
window.addEventListener('load', () => {
    updateAllStatuses();
    setInterval(updateAllStatuses, 30000);

    const container = document.getElementById('aura-3d-container');
    if (container) {
        const root = ReactDOM.createRoot(container);
        root.render(React.createElement(DashboardView));
    }
});