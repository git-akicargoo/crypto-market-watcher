#!/bin/bash

# frontend 디렉토리 생성
rm -rf frontend  # 기존 frontend 삭제
yarn create vite frontend --template react-ts

# frontend 디렉토리로 이동
cd frontend

# 기본 의존성 설치
yarn install

# 필요한 웹소켓 및 기타 패키지 설치
yarn add \
  @stomp/stompjs \
  sockjs-client \
  axios \
  styled-components

# TypeScript 타입 정의 패키지 설치
yarn add -D \
  @types/sockjs-client \
  @types/styled-components

# 설치 완료 메시지
echo "✅ Vite + TypeScript + React 프로젝트 생성 및 패키지 설치가 완료되었습니다."
echo "📁 frontend 디렉토리로 이동하여 작업을 시작하세요."
echo "🚀 프로젝트 시작 명령어: yarn dev"



# cd frontend
# npm install @stomp/stompjs