/**
 * DashboardView.js
 * [클래스 역할] 3D 캔버스 환경 설정 및 전역 상태(status) 감시
 */
(function() {
    const { useState, useEffect, Suspense } = window.React;
    const { Canvas } = window.ReactThreeFiber;
    const { OrbitControls, ContactShadows, Environment } = window.dreidrei;

    window.DashboardView = function DashboardView() {
        // [수정] useState가 정상적으로 동작하도록 보장합니다.
        const [status, setStatus] = useState(200);

        useEffect(() => {
            const checkStatus = setInterval(() => {
                if (window.currentAuraStatus !== undefined) {
                    setStatus(window.currentAuraStatus);
                }
            }, 1000);
            return () => clearInterval(checkStatus);
        }, []);

        return (
            <div style={{ height: '500px', width: '100%', background: '#1a1a1a', borderRadius: '15px' }}>
                <Canvas camera={{ position: [0, 2, 5], fov: 45 }}>
                    <Suspense fallback={null}>
                        <ambientLight intensity={0.5} />
                        <Environment preset="city" />

                        {/* 전역에 등록된 AuraCharacter를 사용합니다. */}
                        <window.AuraCharacter status={status} />

                        <ContactShadows opacity={0.4} scale={10} blur={2} far={4.5} />
                        <OrbitControls enablePan={false} minPolarAngle={Math.PI / 4} maxPolarAngle={Math.PI / 2} />
                    </Suspense>
                </Canvas>
            </div>
        );
    };
})();