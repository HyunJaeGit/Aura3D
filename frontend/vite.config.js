/**
 * [파일 역할] Vite 설정 파일 (Vite Configuration)
 * 이 파일은 리액트 프로젝트의 빌드 방식과 결과물이 저장될 위치를 결정합니다.
 * 스프링 부트의 정적 자원 폴더(static)로 파일을 보내주는 '배달원' 역할을 합니다. [cite: 2025-12-24, 20-29]
 */
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  // 리액트 기능을 사용하기 위한 플러그인 설정 [cite: 2025-12-30]
  plugins: [react()],

  // [추가된 설정] 개발 서버 설정 및 프록시(Proxy)
  // 리액트(5173 포트)에서 보낸 /api 요청을 스프링 부트(8080 포트)로 배달해줍니다.
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },

  build: {
    // [설정 1] 빌드된 결과물이 저장될 폴더 경로 (스프링 부트의 static/dist 폴더)
    // [설정 2] 빌드할 때마다 기존에 있던 낡은 파일들을 삭제합니다.
    // [설정 3] 빌드된 파일의 이름을 고정합니다. (매번 HTML을 수정하지 않기 위해 중요!)
    outDir: '../src/main/resources/static/dist',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        // 자바스크립트 파일 이름을 'index.js'로 고정
        entryFileNames: `assets/[name].js`,
        chunkFileNames: `assets/[name].js`,
        // CSS나 이미지 파일 이름을 고정 [cite: 2025-12-30]
        assetFileNames: `assets/[name].[ext]`
      }
    }
  }
})