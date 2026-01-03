import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

/**
 * [Signup.jsx - 회원가입 컴포넌트]
 * 역할: 사용자로부터 정보를 입력받아 백엔드의 REST API로 전송합니다.
 */
const Signup = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const navigate = useNavigate();

    /**
     * [handleSignup]
     * 폼 제출 시 실행되며, axios를 이용해 데이터를 JSON 형태로 백엔드에 보냅니다.
     */
    const handleSignup = async (e) => {
        e.preventDefault(); // 페이지 새로고침 방지
        try {
            // 백엔드 UserController의 /api/user/join 경로로 데이터 전송
            await axios.post('/api/user/join', {
                email: email,
                password: password,
                name: name
            });
            alert('회원가입이 완료되었습니다! 로그인해 주세요.');
            navigate('/'); // 성공 시 홈 화면으로 이동
        } catch (error) {
            console.error('회원가입 에러:', error);
            alert('회원가입에 실패했습니다. 다시 시도해 주세요.');
        }
    };

    return (
        <div style={{ padding: '100px 50px', maxWidth: '400px', margin: '0 auto', color: '#fff' }}>
            <h2 style={{ color: '#00d4ff', marginBottom: '30px', letterSpacing: '2px' }}>JOIN AURA-3D</h2>
            <form onSubmit={handleSignup}>
                <div style={{ marginBottom: '15px' }}>
                    <input type="text" placeholder="FULL NAME" value={name} onChange={(e) => setName(e.target.value)} style={inputStyle} required />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <input type="email" placeholder="EMAIL ADDRESS" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} required />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <input type="password" placeholder="PASSWORD" value={password} onChange={(e) => setPassword(e.target.value)} style={inputStyle} required />
                </div>
                <button type="submit" style={buttonStyle}>CREATE ACCOUNT</button>
            </form>
        </div>
    );
};

const inputStyle = { width: '100%', padding: '15px', background: '#111', border: '1px solid #333', color: '#fff', borderRadius: '4px', boxSizing: 'border-box' };
const buttonStyle = { width: '100%', padding: '15px', backgroundColor: '#00d4ff', color: '#000', border: 'none', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem' };

export default Signup;