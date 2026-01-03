/**
 * [main.jsx] 리액트 시작점 (Entry Point)
 * HTML의 id="root"인 곳에 리액트 앱을 연결해주는 역할을 합니다.
 */
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'

// StrictMode는 개발 중 잠재적인 문제를 체크해줍니다.
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)