import React, { useState, useEffect } from 'react'; // 1. 필수 Hook 추가
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Signup from './Signup';
import Dashboard from './Dashboard';
import Login from './Login';

/**
 * [App.jsx - 전체 컨트롤러]
 * 역할: 공통 헤더를 구성하고, JWT 세션 만료 시간을 실시간으로 관리하며 경로에 따른 페이지를 렌더링합니다.
 */
function App() {
  // 로컬 스토리지에서 기본 정보 확인
  const [timeLeft, setTimeLeft] = useState(""); // 2. 실시간 남은 시간 상태
  const token = localStorage.getItem('token');
  const userName = localStorage.getItem('userName');
  const userRole = localStorage.getItem('userRole');

  /**
   * [세션 실시간 타이머 로직]
   * 역할: 1초마다 브라우저의 현재 시간과 서버에서 받은 만료 시간(expiresAt)을 비교합니다.
   */
useEffect(() => {
    const expiresAtRaw = localStorage.getItem('expiresAt');

    // 1. 데이터가 없거나 토큰이 없으면 타이머를 돌리지 않고 상태를 초기화합니다.
    if (!expiresAtRaw || !token) {
      setTimeLeft("");
      return;
    }

    // 2. 숫자로 안전하게 변환합니다.
    const expiresAt = Number(expiresAtRaw);

    const timer = setInterval(() => {
      const now = new Date().getTime();
      const distance = expiresAt - now;

      if (distance <= 0) {
        clearInterval(timer);
        localStorage.clear();
        alert("보안 세션이 만료되었습니다. 다시 로그인해주세요.");
        window.location.href = '/login';
      } else {
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        // 3. 계산 결과가 유효한 숫자인지 최종 확인 후 업데이트합니다.
        if (!isNaN(minutes) && !isNaN(seconds)) {
          setTimeLeft(`${minutes}:${seconds < 10 ? '0' : ''}${seconds}`);
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [token]);

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = '/';
  };

  return (
    <Router>
      <div style={{ background: '#000', minHeight: '100vh' }}>
        <nav style={navStyle}>
          <Link to="/" style={{ ...logoStyle, textDecoration: 'none' }}>AURA-3D</Link>
          <div style={{ display: 'flex', gap: '30px', alignItems: 'center' }}>
            <Link to="/" style={linkStyle}>HOME</Link>

            {/* 로그인 상태에 따라 다른 메뉴 및 세션 정보 표시 */}
            {token ? (
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                  <div style={{ color: '#888', fontSize: '0.8rem' }}>
                    name: <strong style={{ color: '#fff' }}>{userName}</strong>
                    <span style={{ color: '#00d4ff', marginLeft: '5px' }}>[{userRole}]</span>
                  </div>

                  {/* --- 세션 만료 시간 표시 박스 --- */}
                  <div style={sessionBoxStyle}>
                    SESSION: {timeLeft}
                  </div>
                </div>

                <button onClick={handleLogout} style={logoutBtnNavStyle}>LOGOUT</button>
                <Link to="/dashboard" style={linkStyle}>DASHBOARD</Link>
              </>
            ) : (
              <>
                <Link to="/signup" style={linkStyle}>SIGNUP</Link>
                <Link to="/login" style={linkStyle}>LOGIN</Link>
              </>
            )}
          </div>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </div>
    </Router>
  );
}

// --- [스타일 설정] ---

const navStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  padding: '25px 50px',
  borderBottom: '1px solid #222',
  alignItems: 'center'
};

const logoStyle = {
  color: '#00d4ff',
  fontSize: '1.4rem',
  fontWeight: 'bold',
  letterSpacing: '2px'
};

const linkStyle = {
  color: '#888',
  textDecoration: 'none',
  fontSize: '0.8rem',
  fontWeight: 'bold',
  transition: '0.3s'
};

const logoutBtnNavStyle = {
  background: 'none',
  border: 'none',
  color: '#ff4444',
  cursor: 'pointer',
  fontSize: '0.8rem',
  fontWeight: 'bold'
};

const sessionBoxStyle = {
  color: '#ffcc00',
  border: '1px solid #ffcc00',
  padding: '2px 8px',
  borderRadius: '4px',
  fontSize: '0.75rem',
  fontWeight: 'bold',
  letterSpacing: '1px'
};

/**
 * [Home 컴포넌트]
 * 역할: 메인 페이지 랜딩 및 로그인 상태별 버튼 분기
 */
const Home = () => {
  const token = localStorage.getItem('token');

  return (
    <div style={{ padding: '150px 20px', textAlign: 'center', color: '#fff' }}>
      <h1 style={{ fontSize: '3.5rem', color: '#00d4ff', letterSpacing: '8px', margin: 0 }}>AURA-3D</h1>
      <p style={{ color: '#888', marginTop: '20px', fontSize: '1.2rem' }}>AI-POWERED 3D MONITORING SYSTEM</p>

      <div style={{ marginTop: '50px', display: 'flex', justifyContent: 'center', gap: '20px' }}>
        {token ? (
          <Link to="/dashboard">
            <button style={mainBtnStyle}>GO TO DASHBOARD</button>
          </Link>
        ) : (
          <>
            <Link to="/signup">
              <button style={mainBtnStyle}>GET STARTED</button>
            </Link>
            <Link to="/login">
              <button style={{ ...mainBtnStyle, background: 'transparent', border: '1px solid #00d4ff', color: '#00d4ff' }}>
                SIGN IN
              </button>
            </Link>
          </>
        )}
      </div>
    </div>
  );
};

const mainBtnStyle = {
  padding: '15px 50px',
  background: '#00d4ff',
  border: 'none',
  fontWeight: 'bold',
  cursor: 'pointer',
  borderRadius: '4px',
  fontSize: '1rem'
};

export default App;