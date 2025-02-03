import { Outlet } from 'react-router-dom';
import WebSocketStatus from './WebSocketStatus';

const Layout = () => {
  return (
    <div>
      <WebSocketStatus />  {/* 모든 페이지 상단에 WebSocket 상태 표시 */}
      <Outlet />  {/* 각 페이지 내용이 여기에 렌더링 */}
    </div>
  );
};

export default Layout;
