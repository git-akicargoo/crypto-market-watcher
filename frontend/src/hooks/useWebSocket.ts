import { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';

// 현재 환경 가져오기 (기본값은 dev)
const ENV = import.meta.env.VITE_ACTIVE_PROFILE || 'dev';
const PREFIX = ENV === 'production' ? 'PROD' : 'DEV';

// 환경에 맞는 WebSocket 설정 가져오기
const WS_PROTOCOL = import.meta.env[`VITE_${PREFIX}_WS_PROTOCOL`];
const WS_HOST = import.meta.env[`VITE_${PREFIX}_WS_HOST`];
const WS_PORT = import.meta.env[`VITE_${PREFIX}_WS_PORT`];
const WS_ENDPOINT = import.meta.env[`VITE_${PREFIX}_WS_ENDPOINT`];

// WebSocket URL 구성
const SOCKET_URL = `${WS_PROTOCOL}://${WS_HOST}:${WS_PORT}${WS_ENDPOINT}`;

console.log('Current Environment:', ENV);
console.log('WebSocket URL:', SOCKET_URL);

export const useWebSocket = () => {
  const [connected, setConnected] = useState(false);
  const [connectTime, setConnectTime] = useState<Date | null>(null);
  const [connectionInfo, setConnectionInfo] = useState<{
    url: string;
    protocol: string;
    readyState: number;
  } | null>(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: SOCKET_URL,
      debug: function (str) {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = function (frame) {
      console.log('[LOG] STOMP 연결 성공', frame);
      setConnected(true);
      setConnectTime(new Date());
      setConnectionInfo({
        url: SOCKET_URL,
        protocol: 'stomp',
        readyState: 1
      });

      // 테스트 구독
      client.subscribe('/subscribe/test', function (message) {
        console.log('받은 메시지:', message.body);
      });

      // 테스트 메시지 전송
      client.publish({
        destination: '/publish/test',
        body: 'Hello from client!'
      });
    };

    client.onDisconnect = function (frame) {
      console.log('[LOG] STOMP 연결 종료', frame);
      setConnected(false);
      setConnectTime(null);
      setConnectionInfo(null);
    };

    client.onStompError = function (frame) {
      console.error('[ERROR] STOMP 에러:', frame);
    };

    client.onWebSocketError = function (event) {
      console.error('[ERROR] WebSocket 에러:', event);
    };

    console.log('Attempting to connect to:', SOCKET_URL);
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return { 
    connected, 
    connectTime, 
    connectionInfo 
  };
};
