import { useEffect, useState } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

const styles = `
  .websocket-status {
    padding: 5px 10px;
    margin: 5px;
    border-radius: 4px;
    font-size: 0.8em;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .websocket-status.connected {
    background-color: #e6ffe6;
  }

  .websocket-status.disconnected {
    background-color: #ffe6e6;
  }

  .status-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    animation: pulse 1.5s infinite;
  }

  .connected .status-indicator {
    background-color: #4CAF50;
  }

  .disconnected .status-indicator {
    background-color: #f44336;
    animation: none;
  }

  .uptime {
    margin-left: auto;
    font-size: 0.75em;
    color: #666;
  }

  .port-info {
    font-size: 0.75em;
    color: #666;
    margin-left: 8px;
  }

  @keyframes pulse {
    0% { transform: scale(1); opacity: 1; }
    50% { transform: scale(1.2); opacity: 0.7; }
    100% { transform: scale(1); opacity: 1; }
  }
`;

const WebSocketStatus = () => {
  const { connected, connectTime, connectionInfo } = useWebSocket();
  const [uptime, setUptime] = useState<string>('');

  useEffect(() => {
    if (!connected || !connectTime) {
      setUptime('');
      return;
    }

    const timer = setInterval(() => {
      const now = new Date();
      const diff = now.getTime() - connectTime.getTime();
      const seconds = Math.floor(diff / 1000);
      const minutes = Math.floor(seconds / 60);
      const hours = Math.floor(minutes / 60);

      setUptime(
        `${hours.toString().padStart(2, '0')}:${(minutes % 60)
          .toString()
          .padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`
      );
    }, 1000);

    return () => clearInterval(timer);
  }, [connected, connectTime]);

  useEffect(() => {
    const styleSheet = document.createElement("style");
    styleSheet.innerText = styles;
    document.head.appendChild(styleSheet);
    return () => {
      document.head.removeChild(styleSheet);
    };
  }, []);

  return (
    <div className={`websocket-status ${connected ? 'connected' : 'disconnected'}`}>
      <div className="status-indicator"></div>
      WebSocket: {connected ? '연결됨' : '연결 안됨'}
      {connectionInfo && (
        <div className="connection-info">
          <div>URL: {connectionInfo.url}</div>
          <div>Protocol: {connectionInfo.protocol}</div>
          <div>상태: {
            connectionInfo.readyState === 0 ? 'CONNECTING' :
            connectionInfo.readyState === 1 ? 'OPEN' :
            connectionInfo.readyState === 2 ? 'CLOSING' :
            'CLOSED'
          }</div>
        </div>
      )}
      {uptime && <div className="uptime">연결 시간: {uptime}</div>}
    </div>
  );
};

export default WebSocketStatus;
