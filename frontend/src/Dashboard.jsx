import React, { useState, useEffect, Suspense, useRef } from 'react'
import { Canvas } from '@react-three/fiber'
import { OrbitControls, useGLTF, Environment, useAnimations, ContactShadows } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'

/**
 * [AuraModel 컴포넌트]
 * 역할: 시스템 상태(정상 200 / 에러 500)에 따라 3D 비서 모델과 애니메이션을 교체하여 렌더링합니다.
 */
function AuraModel({ status }) {
  const group = useRef()

  // 1. 상태에 따른 모델 경로 결정
  const modelPath = status === 200 ? '/models/idle.glb' : '/models/alert.glb'
  const { scene, animations } = useGLTF(modelPath)
  const { actions } = useAnimations(animations, group)

  useEffect(() => {
    // 2. 애니메이션 제어: 모델 로드 시 첫 번째 애니메이션을 부드럽게 재생
    if (actions && Object.keys(actions).length > 0) {
      const firstAction = actions[Object.keys(actions)[0]]
      if (firstAction) {
        firstAction.reset().fadeIn(0.5).play()
      }
    }
    // 3. 클린업: 모델이 바뀔 때 이전 애니메이션을 페이드아웃
    return () => {
      if (actions) {
        Object.values(actions).forEach(action => action?.fadeOut(0.5))
      }
    }
  }, [actions, modelPath])

  return (
    <group ref={group} dispose={null}>
      {/* 3D 모델의 크기와 지면 위 위치 설정 */}
      <primitive object={scene} scale={2.8} position={[0, -2.5, 0]} />
    </group>
  )
}

/**
 * [Dashboard 메인 컴포넌트]
 * 역할: 실시간 모니터링 데이터 시각화, AI 분석 메시지 출력, 3D 환경 제어를 담당합니다.
 */
const Dashboard = () => {
  const navigate = useNavigate()

  // 상태 관리: 서버 상태(status)와 AI 분석 가이드(aiGuide)
  const [monitorData, setMonitorData] = useState({ status: 200, aiGuide: "" })
  const [loading, setLoading] = useState(false)
  const projectId = 1 // 타겟 프로젝트 식별자

  /**
   * [보안 가드]
   * 역할: 로컬 스토리지에 인증 토큰이 없으면 즉시 로그인 페이지로 쫓아냅니다.
   */
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      alert("로그인이 필요한 서비스입니다.")
      navigate('/login')
    }
  }, [navigate])

  /**
   * [실시간 상태 동기화]
   * 역할: 백엔드 API로부터 최신 서버 상태와 AI 가이드 메시지를 수신합니다.
   */
  const updateStatus = async () => {
    setLoading(true)
    try {
      const response = await fetch(`/api/monitoring/status?projectId=${projectId}`)
      if (!response.ok) throw new Error('Network response was not ok')

      const data = await response.json()

      // 성능 최적화: 데이터가 실제로 변했을 때만 상태를 업데이트합니다.
      if (data.status !== monitorData.status || data.aiGuide !== monitorData.aiGuide) {
        setMonitorData(data)
      }
    } catch (error) {
      console.error('상태 업데이트 실패:', error)
      setMonitorData({
        status: 500,
        aiGuide: "시스템 연결 실패. 백엔드 서버와의 연결을 확인하세요."
      })
    } finally {
      setLoading(false)
    }
  }

  // 컴포넌트 마운트 시 최초 실행 및 30초마다 자동 갱신(Polling)
  useEffect(() => {
    updateStatus()
    const timer = setInterval(updateStatus, 30000)
    return () => clearInterval(timer)
  }, [])

  // 서비스 제어 핸들러 (시작/중지)
  const handleStart = async () => {
    try {
      const res = await fetch(`/api/monitoring/start?projectId=${projectId}`, { method: 'POST' })
      if (res.ok) { alert("모니터링 시스템을 가동합니다."); updateStatus(); }
    } catch (err) { console.error(err) }
  }

  const handleStop = async () => {
    try {
      const res = await fetch(`/api/monitoring/stop?projectId=${projectId}`, { method: 'POST' })
      if (res.ok) { alert("모니터링 시스템을 중단합니다."); updateStatus(); }
    } catch (err) { console.error(err) }
  }

  return (
    <main className="system-container" style={containerStyle}>

        {/* --- 1. AI 가이드 말풍선 UI --- */}
        {monitorData.aiGuide && (
          <div className="ai-bubble" style={bubbleStyle}>
            <div style={{ color: '#00d4ff', fontSize: '0.7rem', marginBottom: '8px', fontWeight: 'bold', letterSpacing: '1px' }}>
              GEMINI AI ANALYSIS
            </div>
            <div style={{ color: '#fff', fontSize: '0.85rem', lineHeight: '1.5', wordBreak: 'keep-all' }}>
              "{monitorData.aiGuide}"
            </div>
            <div style={bubbleTailStyle} />
          </div>
        )}

        {/* --- 2. 시스템 상태 표시 헤더 --- */}
        <header style={{ padding: '20px 50px', display: 'flex', justifyContent: 'flex-end', zIndex: 10 }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ color: monitorData.status === 200 ? '#00ff88' : '#ff4444', fontSize: '0.8rem', fontWeight: 'bold', letterSpacing: '1px' }}>
              ● {monitorData.status === 200 ? 'SYSTEM STABLE' : 'CRITICAL ALERT'}
            </div>
          </div>
        </header>

        {/* --- 3. 3D 시각화 영역 (Canvas) --- */}
        <section style={{ flex: 1.2, position: 'relative', overflow: 'hidden' }}>
          <Canvas camera={{ position: [0, 0, 8], fov: 35 }} gl={{ alpha: true }}>

            {/* 배경 이미지와 어울리는 은은한 바닥 격자 (결정 필요 시 삭제 가능) */}
            <gridHelper args={[30, 60, '#004466', '#050505']} position={[0, -2.51, 0]} />

            <ambientLight intensity={2.5} />
            <Environment preset="city" />

            <Suspense fallback={null}>
              <AuraModel status={monitorData.status} />
              {/* 바닥 그림자 설정 */}
              <ContactShadows opacity={0.2} scale={12} blur={3} far={5} />
            </Suspense>

            {/* 줌과 화면 이동을 차단하여 UI 몰입도를 높입니다. */}
            <OrbitControls
                makeDefault
                enableZoom={false}
                enablePan={false}
                minPolarAngle={Math.PI / 2.5}
                maxPolarAngle={Math.PI / 2}
            />
          </Canvas>
        </section>

        {/* --- 4. 하단 프로젝트 콘솔 영역 --- */}
        <footer style={footerStyle}>
          <h2 style={{ fontSize: '0.8rem', color: '#999', marginBottom: '25px', letterSpacing: '1px' }}>PROJECT CONSOLE</h2>
          <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '0 10px' }}>
            <tbody>
              <tr style={{ background: 'rgba(255,255,255,0.02)', borderRadius: '8px' }}>
                <td style={{ padding: '20px', color: '#fff', fontSize: '0.9rem', fontWeight: '500' }}>Naver-Test-Server</td>
                <td style={{ color: monitorData.status === 200 ? '#00ff88' : '#ff4444', fontSize: '0.8rem', fontWeight: 'bold' }}>
                  ● {monitorData.status} OPERATIONAL
                </td>
                <td style={{ textAlign: 'center' }}>
                  <button onClick={handleStart} disabled={loading} style={btnStyle('#00ff88')}>START SERVICE</button>
                  <button onClick={handleStop} disabled={loading} style={btnStyle('#ff4444')}>TERMINATE</button>
                </td>
              </tr>
            </tbody>
          </table>
        </footer>
    </main>
  )
}

/** --- 인라인 스타일 가이드 --- **/

// bg.png를 배경으로 설정하고 스크롤 없는 전체 레이아웃 구성
const containerStyle = {
    width: '100%',
    maxWidth: '1000px',
    height: '90vh',
    margin: '20px auto',
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    boxShadow: '0 0 100px rgba(0,0,0,0.8)',
    backgroundImage: "url('/images/bg.png')", // 배경 이미지 경로
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundColor: '#0a0a0b',
    borderRadius: '8px',
    overflow: 'hidden'
};

const footerStyle = {
    flex: 0.8,
    padding: '40px 50px',
    background: 'rgba(5, 5, 5, 0.85)',
    backdropFilter: 'blur(10px)',
    borderTop: '1px solid rgba(255,255,255,0.05)',
    zIndex: 10
};

const btnStyle = (color) => ({
    background: 'none',
    color: color,
    border: `1px solid ${color}`,
    padding: '8px 20px',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.75rem',
    marginRight: '10px',
    fontWeight: 'bold',
    transition: '0.2s'
});

const bubbleStyle = {
    position: 'absolute',
    top: '100px',
    right: '60px',
    width: '260px',
    padding: '20px',
    background: 'rgba(15, 15, 20, 0.95)',
    border: '1px solid rgba(0, 212, 255, 0.4)',
    borderRadius: '15px',
    zIndex: 100,
    backdropFilter: 'blur(8px)',
    boxShadow: '0 10px 30px rgba(0,0,0,0.5)'
};

const bubbleTailStyle = {
    position: 'absolute',
    left: '-10px',
    top: '30px',
    borderTop: '10px solid transparent',
    borderBottom: '10px solid transparent',
    borderRight: '10px solid rgba(0, 212, 255, 0.4)'
};

export default Dashboard