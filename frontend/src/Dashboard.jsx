import React, { useState, useEffect, Suspense, useRef } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, useGLTF, Environment, useAnimations, ContactShadows } from '@react-three/drei';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

/**
 * [AuraModel 컴포넌트]
 * 역할: 시스템 상태(200/500)에 따라 모델 파일과 애니메이션을 교체합니다.
 */
function AuraModel({ status }) {
  const group = useRef();
  const modelPath = status === 200 ? '/models/idle.glb' : '/models/alert.glb';
  const { scene, animations } = useGLTF(modelPath);
  const { actions } = useAnimations(animations, group);

  useEffect(() => {
    if (actions && Object.keys(actions).length > 0) {
      const firstAction = actions[Object.keys(actions)[0]];
      if (firstAction) {
        firstAction.reset().fadeIn(0.5).play();
      }
    }
    return () => {
      if (actions) {
        Object.values(actions).forEach(action => action?.fadeOut(0.5));
      }
    };
  }, [actions, modelPath]);

  return (
    <group ref={group} dispose={null}>
      <primitive object={scene} scale={2.8} position={[0, -2.5, 0]} />
    </group>
  );
}

const Dashboard = () => {
  const navigate = useNavigate();

  // --- [상태 관리] ---
  const [projects, setProjects] = useState([]);
  const [newProject, setNewProject] = useState({ name: '', url: '' });
  const [monitorData, setMonitorData] = useState({ status: 200, aiGuide: "" });
  const [loading, setLoading] = useState(false);
  const [selectedProjectId, setSelectedProjectId] = useState(null);
  const userName = localStorage.getItem('userName') || '관리자';

  // --- [중복 호출 방지용 Ref] ---
  const prevStatusRef = useRef(200); // 이전 시스템 상태 저장
  const hasGreetedRef = useRef(false); // 인사말 실행 여부 저장

  /**
   * [1. 데이터 로드: 프로젝트 목록 및 상태 변화 감지]
   */
const fetchProjects = async () => {
  try {
    const response = await axios.get('/api/projects/list');
    const currentProjects = response.data;
    setProjects(currentProjects);

    const hasError = currentProjects.some(p => p.lastStatus !== 200 && p.lastStatus !== 0);
    const currentStatus = hasError ? 500 : 200;

    // 1. 시스템 상태 변화 감지 및 AI 분석 호출
    if (prevStatusRef.current !== currentStatus) {
      // [최적화] 사용자가 특정 프로젝트를 클릭해서 '상세 분석'을 보고 있는 중이 아닐 때만 전체 상태 분석 호출
      if (!selectedProjectId) {
        fetchAiAnalysisByStatus(currentStatus);
      }
      prevStatusRef.current = currentStatus;
    }

    // 2. [핵심 수정] monitorData 업데이트 시 기존 aiGuide를 보존합니다.
    setMonitorData(prev => ({
      ...prev,
      status: currentStatus,
      aiGuide: prev.aiGuide // 기존에 떠있던 인사말이나 분석 결과를 유지함
    }));

  } catch (error) {
    console.error("목록 로드 실패:", error);
  }
};

  /**
   * [2. AI 인사말 로드 - 최초 1회만 실행]
   */
  const fetchGreeting = async () => {
    if (hasGreetedRef.current) return; // 이미 인사를 했다면 실행하지 않음
    try {
      const response = await axios.get(`/api/monitoring/welcome?userName=${userName}`);
      setMonitorData(prev => ({ ...prev, aiGuide: response.data }));
      hasGreetedRef.current = true; // 인사 완료 표시
    } catch (error) {
      console.error("인사말 로드 실패:", error);
    }
  };

  /**
   * [3. 상태 변화에 따른 시스템 전체 AI 분석]
   */
  const fetchAiAnalysisByStatus = async (status) => {
    try {
      // 백엔드에 statusCode를 보내 적절한 가이드를 요청합니다.
      const response = await axios.get(`/api/monitoring/analyze_status?statusCode=${status}`);
      setMonitorData(prev => ({ ...prev, aiGuide: response.data }));
    } catch (error) {
      console.error("상태 분석 실패:", error);
    }
  };

  /**
   * [4. 특정 프로젝트 클릭, 해제 시 상세 AI 분석]
   */
const handleProjectClick = async (project) => {
  // 1. 이미 선택된 행을 다시 클릭했는지 확인
  if (selectedProjectId === project.id) {
    console.log("선택 해제: 시스템 전체 분석 상태로 복구");
    setSelectedProjectId(null); // 선택 해제

    // 선택이 해제되면 다시 시스템 전체 상태에 맞는 가이드를 보여줌
    fetchAiAnalysisByStatus(monitorData.status);
    return; // 함수 종료
  }

  // 2. 새로운 행을 클릭했을 경우 (기존 로직)
  setSelectedProjectId(project.id);
  try {
    const response = await axios.get(`/api/monitoring/analyze?projectId=${project.id}`);
    setMonitorData(prev => ({ ...prev, aiGuide: response.data }));
  } catch (error) {
    console.error("분석 실패:", error);
    setMonitorData(prev => ({ ...prev, aiGuide: "해당 프로젝트를 분석할 수 없습니다." }));
  }
};

  // --- [핸들러: 등록, 제어, 삭제] ---
  const handleAddProject = async (e) => {
    e.preventDefault();
    if (!newProject.name || !newProject.url) return;
    try {
      await axios.post('/api/projects/add', newProject);
      setNewProject({ name: '', url: '' });
      fetchProjects();
    } catch (error) { alert("등록 실패"); }
  };

  const handleToggleService = async (id, action) => {
    setLoading(true);
    try {
      await axios.post(`/api/monitoring/${action}?projectId=${id}`);
      fetchProjects();
    } finally { setLoading(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await axios.delete(`/api/projects/${id}`);
      fetchProjects();
    } catch (error) { alert("삭제 실패"); }
  };

  /**
   * [메인 이펙트: 초기 로드 및 타이머 설정]
   */
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    fetchGreeting(); // 최초 1회 인사
    fetchProjects(); // 초기 데이터 로드

    const timer = setInterval(fetchProjects, 60000); // 1분 간격 갱신
    return () => clearInterval(timer);
  }, [navigate]);

  return (
    <main className="system-container" style={containerStyle}>
      {/* AI 가이드 말풍선 */}
      {monitorData.aiGuide && (
        <div className="ai-bubble" style={bubbleStyle}>
          <div style={{ color: '#00d4ff', fontSize: '0.7rem', marginBottom: '8px', fontWeight: 'bold' }}>GEMINI AI ANALYSIS</div>
          <div style={{ color: '#fff', fontSize: '0.85rem', lineHeight: '1.5' }}>"{monitorData.aiGuide}"</div>
          <div style={bubbleTailStyle} />
        </div>
      )}

      {/* 헤더 및 시스템 상태 표시 */}
      <header style={{ padding: '20px 50px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', zIndex: 10 }}>
        <form onSubmit={handleAddProject} style={{ display: 'flex', gap: '10px' }}>
          <input style={inputStyle} placeholder="프로젝트 명" value={newProject.name} onChange={e => setNewProject({...newProject, name: e.target.value})} />
          <input style={inputStyle} placeholder="URL (https://...)" value={newProject.url} onChange={e => setNewProject({...newProject, url: e.target.value})} />
          <button type="submit" style={btnStyle('#00d4ff')}>ADD PROJECT</button>
        </form>
        <div style={{ color: monitorData.status === 200 ? '#00ff88' : '#ff4444', fontSize: '0.8rem', fontWeight: 'bold' }}>
          ● {monitorData.status === 200 ? 'SYSTEM STABLE' : 'CRITICAL ALERT'}
        </div>
      </header>

      {/* 3D 모델 캔버스 영역 */}
      <section style={{ flex: 1.2, position: 'relative' }}>
        <Canvas camera={{ position: [0, 0, 8], fov: 35 }}>
          <gridHelper args={[30, 60, '#004466', '#050505']} position={[0, -2.51, 0]} />
          <ambientLight intensity={2.5} />
          <Environment preset="city" />
          <Suspense fallback={null}>
            <AuraModel status={monitorData.status} />
            <ContactShadows opacity={0.2} scale={12} blur={3} far={5} />
          </Suspense>
          <OrbitControls enableZoom={false} enablePan={false} minPolarAngle={Math.PI / 2.5} maxPolarAngle={Math.PI / 2} />
        </Canvas>
      </section>

      {/* 하단 프로젝트 리스트 콘솔 */}
      <footer style={footerStyle}>
        <h2 style={{ fontSize: '0.8rem', color: '#999', marginBottom: '20px' }}>PROJECT CONSOLE <span style={{fontSize: '0.6rem', marginLeft: '10px'}}>(행을 클릭하여 AI 분석 실행)</span></h2>
        <div style={{ maxHeight: '200px', overflowY: 'auto', paddingRight: '10px' }}>
          <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '0 10px' }}>
            <tbody>
              {projects.length === 0 ? (
                <tr><td style={{color: '#666', fontSize: '0.8rem'}}>등록된 프로젝트가 없습니다.</td></tr>
              ) : (
                projects.map((project) => (
                  <tr
                    key={project.id}
                    onClick={() => handleProjectClick(project)}
                    style={{
                      background: selectedProjectId === project.id ? 'rgba(0, 212, 255, 0.1)' : 'rgba(255,255,255,0.02)',
                      borderRadius: '8px', cursor: 'pointer', transition: '0.2s'
                    }}
                  >
                    <td style={{ padding: '15px 20px', color: '#00d4ff', fontSize: '0.9rem', fontWeight: 'bold' }}> # {project.id} </td>
                    <td style={{ padding: '15px 20px', color: '#fff', fontSize: '0.9rem' }}>
                      {project.name}
                      <div style={{ fontSize: '0.7rem', color: '#555' }}>{project.url}</div>
                    </td>
                    <td style={{ color: project.lastStatus === 200 ? '#00ff88' : '#ff4444', fontSize: '0.8rem', fontWeight: 'bold', width: '80px' }}>
                       {project.lastStatus === 0 ? '---' : project.lastStatus}
                    </td>
                    <td style={{ textAlign: 'right', paddingRight: '20px' }}>
                      <button onClick={(e) => { e.stopPropagation(); handleToggleService(project.id, 'start'); }} disabled={loading} style={btnStyle('#00ff88')}>START</button>
                      <button onClick={(e) => { e.stopPropagation(); handleToggleService(project.id, 'stop'); }} disabled={loading} style={btnStyle('#ffbb00')}>STOP</button>
                      <button onClick={(e) => { e.stopPropagation(); handleDelete(project.id); }} style={btnStyle('#ff4444')}>REMOVE</button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </footer>
    </main>
  );
};

// --- [스타일 설정 생략: 기존과 동일] ---
const containerStyle = { width: '100%', maxWidth: '1000px', height: '90vh', margin: '20px auto', position: 'relative', display: 'flex', flexDirection: 'column', boxShadow: '0 0 100px rgba(0,0,0,0.8)', backgroundImage: "url('/images/bg.png')", backgroundSize: 'cover', backgroundPosition: 'center', backgroundColor: '#0a0a0b', borderRadius: '8px', overflow: 'hidden' };
const footerStyle = { flex: 0.8, padding: '30px 50px', background: 'rgba(5, 5, 5, 0.85)', backdropFilter: 'blur(10px)', borderTop: '1px solid rgba(255,255,255,0.05)', zIndex: 10 };
const inputStyle = { background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', padding: '8px 12px', borderRadius: '4px', fontSize: '0.75rem', outline: 'none' };
const btnStyle = (color) => ({ background: 'none', color: color, border: `1px solid ${color}`, padding: '5px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', marginLeft: '5px', transition: '0.2s' });
const bubbleStyle = { position: 'absolute', top: '100px', right: '60px', width: '260px', padding: '20px', background: 'rgba(15, 15, 20, 0.95)', border: '1px solid rgba(0, 212, 255, 0.4)', borderRadius: '15px', zIndex: 100, backdropFilter: 'blur(8px)' };
const bubbleTailStyle = { position: 'absolute', left: '-10px', top: '30px', borderTop: '10px solid transparent', borderBottom: '10px solid transparent', borderRight: '10px solid rgba(0, 212, 255, 0.4)' };

export default Dashboard;