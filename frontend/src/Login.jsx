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

        // 백엔드에서 넘어온 전체 데이터를 로그로 찍어보세요. (F12 Console 탭 확인)
        console.log("로그인 응답 상세:", response.data);

        const { token, name, role, expiresAt } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('userName', name);
        localStorage.setItem('userRole', role);

        // 숫자가 올바르게 들어왔을 때만 저장합니다.
        if (expiresAt) {
            localStorage.setItem('expiresAt', String(expiresAt));
        } else {
            console.error("만료 시각(expiresAt) 데이터가 응답에 포함되지 않았습니다.");
        }

        alert(`${name}님, 환영합니다!`);
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