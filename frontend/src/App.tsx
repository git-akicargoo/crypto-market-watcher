import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import MainPages from './pages/MainPages';

const routes = [
  {
    path: '/',
    element: <MainPages />
  }
];

const App = () => {
  return (
    <Router>
      <Routes>
        <Route element={<Layout />}>
          {routes.map((route) => (
            <Route
              key={route.path}
              path={route.path}
              element={route.element}
            />
          ))}
        </Route>
      </Routes>
    </Router>
  );
};

export default App;
