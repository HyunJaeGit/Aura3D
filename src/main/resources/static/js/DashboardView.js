/**
 * DashboardView.js
 * [역할] 3D 화면을 생성하고, dashboard.js가 전달해주는 상태값을 AuraCharacter에 전달합니다.
 */
import React, { useState, useEffect, Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, ContactShadows, Environment } from '@react-three/drei';
import { AuraCharacter } from './AuraCharacter';

export default function DashboardView() {
    const [status, setStatus] = useState(200);

    useEffect(() => {
        // dashboard.js가 업데이트하는 window.currentAuraStatus를 감시합니다
        const checkStatus = setInterval(() => {
            if (window.currentAuraStatus !== undefined) {
                setStatus(window.currentAuraStatus);
            }
        }, 1000); // 1초마다 체크
        return () => clearInterval(checkStatus);
    }, []);

    return (
        <div style={{ height: '500px', width: '100%', background: '#1a1a1a', borderRadius: '15px', overflow: 'hidden' }}>
            <Canvas camera={{ position: [0, 2, 5], fov: 45 }}>
                <Suspense fallback={null}>
                    <ambientLight intensity={0.5} />
                    <Environment preset="city" /> {/* 고급 조명 설정 */}

                    <AuraCharacter status={status} />

                    <ContactShadows opacity={0.4} scale={10} blur={2} far={4.5} />
                    <OrbitControls enablePan={false} minPolarAngle={Math.PI / 4} maxPolarAngle={Math.PI / 2} />
                </Suspense>
            </Canvas>
        </div>
    );
}