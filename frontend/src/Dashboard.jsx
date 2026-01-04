import React, { useState, useEffect, Suspense, useRef } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, useGLTF, Environment, useAnimations, ContactShadows } from '@react-three/drei';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

/**
 * [AuraModel 컴포넌트]
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
  const [selectedProjectId, setSelectedProjectId] = useState(null); // 추가: 선택된 프로젝트 관리
  const userName = localStorage.getItem('userName') || '관리자';

  /**
   * [1. 데이터 로드: 프로젝트 목록]
   */
  const fetchProjects = async () => {
    try {
      const response = await axios.get('/api/projects/list');
      setProjects(response.data);
      const hasError = response.data.some(p => p.lastStatus !== 200 && p.lastStatus !== 0);
      setMonitorData(prev => ({ ...prev, status: hasError ? 500 : 200 }));
    } catch (error) {
      console.error("목록 로드 실패:", error);
    }
  };

  /**
   * [2. AI 인사말 로드]
   */
  const fetchGreeting = async () => {
    try {
      const response = await axios.get(`/api/monitoring/welcome?userName=${userName}`);
      setMonitorData(prev => ({ ...prev, aiGuide: response.data }));
    } catch (error) {
      console.error("인사말 로드 실패:", error);
    }
  };

  /**
   * [추가 기능: 특정 프로젝트 AI 분석 가이드 요청]
   */
  const handleProjectClick = async (project) => {
    setSelectedProjectId(project.id);
    try {
      // 백엔드의 새로운 분석 엔드포인트 호출
      const response = await axios.get(`/api/monitoring/analyze?projectId=${project.id}`);
      setMonitorData(prev => ({ ...prev, aiGuide: response.data }));
    } catch (error) {
      console.error("분석 실패:", error);
      setMonitorData(prev => ({ ...prev, aiGuide: "분석 데이터를 가져오지 못했습니다." }));
    }
  };

  /**
   * [3. 신규 프로젝트 등록]
   */
  const handleAddProject = async (e) => {
    e.preventDefault();
    if (!newProject.name || !newProject.url) {
      alert("이름과 URL을 모두 입력해주세요.");
      return;
    }
    try {
      await axios.post('/api/projects/add', newProject);
      alert("성공적으로 등록되었습니다!");
      setNewProject({ name: '', url: '' });
      fetchProjects();
    } catch (error) {
      alert(error.response?.data?.message || "등록에 실패했습니다.");
    }
  };

  /**
   * [4. 감시 제어]
   */
  const handleToggleService = async (id, action) => {
    setLoading(true);
    try {
      const res = await axios.post(`/api/monitoring/${action}?projectId=${id}`);
      if (res.status === 200) {
        alert(`프로젝트 #${id} 감시를 ${action === 'start' ? '시작' : '중단'}합니다.`);
        fetchProjects();
      }
    } catch (err) {
      console.error("제어 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * [5. 프로젝트 삭제]
   */
  const handleDelete = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await axios.delete(`/api/projects/${id}`);
      fetchProjects();
    } catch (error) {
      alert("삭제 실패");
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert("로그인이 필요합니다.");
      navigate('/login');
      return;
    }
    fetchGreeting();
    fetchProjects();
    const timer = setInterval(fetchProjects, 10000);
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

      {/* 헤더 */}
      <header style={{ padding: '20px 50px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', zIndex: 10 }}>
        <form onSubmit={handleAddProject} style={{ display: 'flex', gap: '10px' }}>
          <input
            style={inputStyle} placeholder="프로젝트 명"
            value={newProject.name} onChange={e => setNewProject({...newProject, name: e.target.value})}
          />
          <input
            style={inputStyle} placeholder="URL (https://...)"
            value={newProject.url} onChange={e => setNewProject({...newProject, url: e.target.value})}
          />
          <button type="submit" style={btnStyle('#00d4ff')}>ADD PROJECT</button>
        </form>
        <div style={{ color: monitorData.status === 200 ? '#00ff88' : '#ff4444', fontSize: '0.8rem', fontWeight: 'bold' }}>
          ● {monitorData.status === 200 ? 'SYSTEM STABLE' : 'CRITICAL ALERT'}
        </div>
      </header>

      {/* 3D 영역 */}
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

      {/* 하단 리스트 */}
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
                    onClick={() => handleProjectClick(project)} // 행 클릭 이벤트
                    style={{
                      background: selectedProjectId === project.id ? 'rgba(0, 212, 255, 0.1)' : 'rgba(255,255,255,0.02)',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      transition: '0.2s'
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

// --- [스타일 설정] ---
const containerStyle = {
    width: '100%', maxWidth: '1000px', height: '90vh', margin: '20px auto',
    position: 'relative', display: 'flex', flexDirection: 'column',
    boxShadow: '0 0 100px rgba(0,0,0,0.8)', backgroundImage: "url('/images/bg.png')",
    backgroundSize: 'cover', backgroundPosition: 'center', backgroundColor: '#0a0a0b',
    borderRadius: '8px', overflow: 'hidden'
};

const footerStyle = {
    flex: 0.8, padding: '30px 50px', background: 'rgba(5, 5, 5, 0.85)',
    backdropFilter: 'blur(10px)', borderTop: '1px solid rgba(255,255,255,0.05)', zIndex: 10
};

const inputStyle = {
    background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)',
    color: '#fff', padding: '8px 12px', borderRadius: '4px', fontSize: '0.75rem', outline: 'none'
};

const btnStyle = (color) => ({
    background: 'none', color: color, border: `1px solid ${color}`,
    padding: '5px 12px', borderRadius: '4px', cursor: 'pointer',
    fontSize: '0.65rem', fontWeight: 'bold', marginLeft: '5px', transition: '0.2s'
});

const bubbleStyle = {
    position: 'absolute', top: '100px', right: '60px', width: '260px', padding: '20px',
    background: 'rgba(15, 15, 20, 0.95)', border: '1px solid rgba(0, 212, 255, 0.4)',
    borderRadius: '15px', zIndex: 100, backdropFilter: 'blur(8px)'
};

const bubbleTailStyle = {
    position: 'absolute', left: '-10px', top: '30px',
    borderTop: '10px solid transparent', borderBottom: '10px solid transparent',
    borderRight: '10px solid rgba(0, 212, 255, 0.4)'
};

export default Dashboard;