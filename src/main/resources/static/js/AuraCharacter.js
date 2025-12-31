/**
 * AuraCharacter.js
 * [클래스 역할] 상태값에 따라 GLB 모델의 애니메이션을 제어합니다.
 */
(function() {
    // CDN에서 제공하는 전역 변수에서 필요한 함수를 직접 추출합니다.
    const { useEffect, useRef } = window.React;
    const { useGLTF, useAnimations } = window.dreidrei;

    // 전역에서 접근 가능하도록 window 객체에 할당합니다 (export 대체)
    window.AuraCharacter = function AuraCharacter({ status }) {
        const group = useRef();

        // 모델 로드 (경로: /static/assets/...)
        const { nodes, animations: idleAnims } = useGLTF('/assets/idle.glb');
        const { animations: alertAnims } = useGLTF('/assets/alert.glb');

        // 모든 애니메이션을 합쳐서 관리합니다.
        const { actions } = useAnimations([...idleAnims, ...alertAnims], group);

        useEffect(() => {
            if (!actions) return;

            // 200이면 idle, 아니면 alert 애니메이션 선택
            const animName = status === 200 ? idleAnims[0].name : alertAnims[0].name;

            if (actions[animName]) {
                // 이전 애니메이션을 멈추고 새 애니메이션을 재생합니다.
                Object.values(actions).forEach(action => action.fadeOut(0.5));
                actions[animName].reset().fadeIn(0.5).play();
            }
        }, [status, actions]);

        return (
            <group ref={group} dispose={null} scale={[1.5, 1.5, 1.5]}>
                <primitive object={nodes.Scene} />
            </group>
        );
    };

    // 프리로딩 설정
    useGLTF.preload('/assets/idle.glb');
    useGLTF.preload('/assets/alert.glb');
})();