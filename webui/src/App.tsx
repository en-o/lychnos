import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import AIAnalysisSettingsPage from './pages/AIAnalysisSettingsPage';
import AIImageSettingsPage from './pages/AIImageSettingsPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import PrivateRoute from './components/PrivateRoute';

function App() {
  return (
    <Router>
      <Routes>
        {/* 公开页面 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<HomePage />} />

        {/* 需要登录的页面 */}
        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <ProfilePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings/ai-analysis"
          element={
            <PrivateRoute>
              <AIAnalysisSettingsPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings/ai-image"
          element={
            <PrivateRoute>
              <AIImageSettingsPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings/password"
          element={
            <PrivateRoute>
              <ChangePasswordPage />
            </PrivateRoute>
          }
        />

        {/* 404 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
