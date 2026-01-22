import {HashRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import HistoryPage from './pages/HistoryPage';
import PreferencePage from './pages/PreferencePage';
import ModelSettingsPage from './pages/ModelSettingsPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import AdminOAuthConfigPage from './pages/AdminOAuthConfigPage';
import AdminUserPage from './pages/AdminUserPage';
import AdminAIModelPage from './pages/AdminAIModelPage';
import SystemManagePage from './pages/SystemManagePage';
import PrivateRoute from './components/PrivateRoute';
import ToastContainer from './components/ToastContainer';

function App() {
  return (
    <Router>
      <ToastContainer />
      <Routes>
        {/* 公开页面 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
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
          path="/history"
          element={
            <PrivateRoute>
              <HistoryPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/preference"
          element={
            <PrivateRoute>
              <PreferencePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings/models"
          element={
            <PrivateRoute>
              <ModelSettingsPage />
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

        {/* 系统管理页面 */}
        <Route
          path="/sys-manage"
          element={
            <PrivateRoute>
              <SystemManagePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/sys-manage/oauth-config"
          element={
            <PrivateRoute>
              <AdminOAuthConfigPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/sys-manage/users"
          element={
            <PrivateRoute>
              <AdminUserPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/sys-manage/ai-model"
          element={
            <PrivateRoute>
              <AdminAIModelPage />
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
