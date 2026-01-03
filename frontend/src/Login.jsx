import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('/api/user/login', { email, password });

            // 1. 서버가 보낸 객체 데이터 구조 분해 할당
            const { token, name, role } = response.data;

            // 2. 브라우저 로컬 스토리지에 저장 (나중에 대시보드에서 쓰기 위함)
            localStorage.setItem('token', token);
            localStorage.setItem('userName', name);
            localStorage.setItem('userRole', role);

            // 3. 알림창에 객체가 아닌 '이름'이 나오도록 수정
            alert(`${name}님, 환영합니다!`);

            // 4. 대시보드로 이동
            window.location.href = '/dashboard';
        } catch (error) {
            console.error('로그인 에러:', error);
            alert('로그인에 실패했습니다. 정보를 확인하세요.');
        }
    };

    return (
        <div style={{ padding: '100px 50px', maxWidth: '400px', margin: '0 auto', color: '#fff' }}>
            <h2 style={{ color: '#00d4ff', marginBottom: '30px' }}>LOGIN AURA-3D</h2>
            <form onSubmit={handleLogin}>
                <input type="email" placeholder="EMAIL" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} required />
                <input type="password" placeholder="PASSWORD" value={password} onChange={(e) => setPassword(e.target.value)} style={inputStyle} required />
                <button type="submit" style={buttonStyle}>SIGN IN</button>
            </form>
        </div>
    );
};

const inputStyle = { width: '100%', padding: '15px', marginBottom: '15px', background: '#111', border: '1px solid #333', color: '#fff', borderRadius: '4px' };
const buttonStyle = { width: '100%', padding: '15px', backgroundColor: '#00d4ff', color: '#000', border: 'none', cursor: 'pointer', fontWeight: 'bold' };

export default Login;