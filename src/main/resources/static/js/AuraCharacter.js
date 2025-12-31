/**
 * AuraCharacter.js
 * [역할] 상태값(status)을 받아 그에 맞는 3D 애니메이션(Idle/Alert)을 재생합니다.
 */
import React, { useEffect, useRef } from 'react';
import { useGLTF, useAnimations } from '@react-three/drei';

export function AuraCharacter({ status }) {
    const group = useRef();

    // 1. 모델과 애니메이션 정보 로드 (정상/장애 상태용 2개 로드)
    const { nodes, materials, animations: idleAnims } = useGLTF('/assets/idle.glb');
    const { animations: alertAnims } = useGLTF('/assets/alert.glb');

    // 2. 애니메이션 제어 도구 설정
    const { actions } = useAnimations([...idleAnims, ...alertAnims], group);

    useEffect(() => {
        // 3. 상태(status)가 200이면 idle, 아니면 alert 애니메이션 이름 선택
        const animName = status === 200 ? idleAnims[0].name : alertAnims[0].name;

        // 기존 동작 부드럽게 멈추고 새로운 동작 실행
        actions[animName]?.reset().fadeIn(0.5).play();

        return () => actions[animName]?.fadeOut(0.5);
    }, [status, actions, idleAnims, alertAnims]);

    return (
        <group ref={group} dispose={null} scale={[1.5, 1.5, 1.5]}>
            {/* 믹사모에서 가져온 전체 씬을 표시합니다 */}
            <primitive object={nodes.Scene} />
        </group>
    );
}

// 성능 최적화를 위한 프리로드
useGLTF.preload('/assets/idle.glb');
useGLTF.preload('/assets/alert.glb');