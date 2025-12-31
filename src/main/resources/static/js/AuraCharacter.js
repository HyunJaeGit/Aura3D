/**
 AuraCharater.js
 * [클래스 역할]
 * AuraCharacter: .glb 파일을 로드하여 3D 화면에 표시하는 컴포넌트입니다.
 * 나중에 백엔드 statusCode에 따라 애니메이션(Idle/Alert)을 바꿀 '몸체'가 됩니다.
 */
import React, { useRef } from 'react';
import { useGLTF } from '@react-three/drei';

export function AuraCharacter(props) {
    // 1. useGLTF: 우리가 저장한 파일을 불러옵니다.
    // 경로는 스프링 부트의 static 기준이므로 /assets/파일명 으로 적습니다.
    const { nodes, materials } = useGLTF('/assets/aura_assistant.glb');

    // 2. group: 여러 부품으로 나뉘어 있을 수 있는 캐릭터를 하나로 묶습니다.
    const group = useRef();

    return (
        <group ref={group} {...props} dispose={null}>
            {/* nodes.male: 블렌더에서 이름지은 'male' 객체를 가져옵니다.
                geometry와 material은 블렌더에서 설정한 모양과 질감을 의미합니다.
            */}
            <mesh
                geometry={nodes.male.geometry}
                material={materials['Material.001'] || materials.default} // 재질 이름이 다를 수 있어 기본값 설정
                scale={[1, 1, 1]} // 필요시 크기 조절
            />
        </group>
    );
}

// 성능을 위해 미리 로드해둡니다.
useGLTF.preload('/assets/aura_assistant.glb');