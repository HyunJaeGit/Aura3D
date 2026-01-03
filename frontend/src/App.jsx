import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Signup from './Signup';
import Dashboard from './Dashboard';
import Login from './Login';

/**
 * [App.jsx - 전체 컨트롤러]
 * 역할: 공통 헤더를 구성하고, URL 경로에 따라 다른 페이지를 렌더링합니다.
 */
function App() {
    // 로컬 스토리지에서 정보 확인
      const token = localStorage.getItem('token');
      const userName = localStorage.getItem('userName');
      const userRole = localStorage.getItem('userRole');

      const handleLogout = () => {
        localStorage.clear();
        window.location.href = '/'; // 페이지 전체를 새로고침하며 홈으로 이동
      };

  return (
      <Router>
        <div style={{ background: '#000', minHeight: '100vh' }}>
          <nav style={navStyle}>
            <Link to="/" style={{ ...logoStyle, textDecoration: 'none' }}>AURA-3D</Link>
            <div style={{ display: 'flex', gap: '30px', alignItems: 'center' }}>
              <Link to="/" style={linkStyle}>HOME</Link>
              <Link to="/signup" style={linkStyle}>SIGNUP</Link>

              {/* 로그인 상태에 따라 다른 메뉴 표시 */}
              {token ? (
                <>
                  <div style={{ color: '#888', fontSize: '0.8rem' }}>
                    name: <strong style={{ color: '#fff' }}>{userName}</strong>
                    <span style={{ color: '#00d4ff', marginLeft: '5px' }}>[{userRole}]</span>
                  </div>
                  <button onClick={handleLogout} style={logoutBtnNavStyle}>LOGOUT</button>
                  <Link to="/dashboard" style={linkStyle}>DASHBOARD</Link>
                </>
              ) : (
                <Link to="/login" style={linkStyle}>LOGIN</Link>
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

  // 로그아웃 버튼용 스타일 추가
  const logoutBtnNavStyle
  = { background: 'none', border: 'none', color: '#ff4444',
      cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold' };

/**
 * [Home.jsx - 중앙 버튼 조건부 렌더링 수정]
 * 역할: 로그인 상태(token 존재 여부)에 따라 중앙 버튼 구성을 변경합니다.
 */
const Home = () => {
  // 1. 로컬 스토리지에서 토큰 확인
  const token = localStorage.getItem('token');

  return (
    <div style={{ padding: '150px 20px', textAlign: 'center', color: '#fff' }}>
      <h1 style={{ fontSize: '3.5rem', color: '#00d4ff', letterSpacing: '8px', margin: 0 }}>AURA-3D</h1>
      <p style={{ color: '#888', marginTop: '20px', fontSize: '1.2rem' }}>AI-POWERED 3D MONITORING SYSTEM</p>

      <div style={{ marginTop: '50px', display: 'flex', justifyContent: 'center', gap: '20px' }}>
        {/* 2. 로그인 상태에 따른 버튼 처리 */}
        {token ? (
          // 로그인 된 경우: 대시보드로 바로가기 버튼 표시
          <Link to="/dashboard">
            <button style={mainBtnStyle}>GO TO DASHBOARD</button>
          </Link>
        ) : (
          // 로그인 안 된 경우: 기존 회원가입/로그인 버튼 표시
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


// 전역 스타일 설정
const navStyle = { display: 'flex', justifyContent: 'space-between', padding: '25px 50px', borderBottom: '1px solid #222', alignItems: 'center' };
const logoStyle = { color: '#00d4ff', fontSize: '1.4rem', fontWeight: 'bold', letterSpacing: '2px' };
const linkStyle = { color: '#888', textDecoration: 'none', fontSize: '0.8rem', fontWeight: 'bold', transition: '0.3s' };
const mainBtnStyle = { padding: '15px 50px', background: '#00d4ff', border: 'none', fontWeight: 'bold', cursor: 'pointer', borderRadius: '4px', fontSize: '1rem' };

export default App;