import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen } from 'lucide-react';
import { mockAuthApi } from '../api/auth';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username || !password) {
      alert('请输入用户名和密码');
      return;
    }

    setLoading(true);

    try {
      const response = await mockAuthApi.login({ username, password });

      if (response.success && response.data) {
        // 保存token
        localStorage.setItem('token', response.data.token);

        // 获取用户信息
        const userInfoRes = await mockAuthApi.getUserInfo();
        if (userInfoRes.success) {
          localStorage.setItem('userInfo', JSON.stringify(userInfoRes.data));
        }

        // 跳转到主页
        navigate('/');
      } else {
        alert(response.message);
      }
    } catch (error: any) {
      console.error('登录失败:', error);
      alert(error.response?.data?.message || '登录失败,请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md">
        {/* Logo和标题 */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-3">
            <BookOpen className="w-10 h-10 text-indigo-600" />
            <h1 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              图书阅读助理
            </h1>
          </div>
          <p className="text-gray-600">AI 驱动的个性化阅读推荐系统</p>
        </div>

        {/* 登录表单 */}
        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              用户名
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-indigo-500 transition"
              placeholder="请输入用户名"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-indigo-500 transition"
              placeholder="请输入密码"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full px-6 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-medium shadow-lg"
          >
            {loading ? (
              <div className="flex items-center justify-center gap-2">
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                登录中...
              </div>
            ) : (
              '登录'
            )}
          </button>
        </form>

        {/* 提示信息 */}
        <div className="mt-6 p-4 bg-indigo-50 rounded-xl">
          <p className="text-sm text-indigo-900 font-medium mb-1">演示账号:</p>
          <p className="text-sm text-indigo-700">用户名: admin</p>
          <p className="text-sm text-indigo-700">密码: admin</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
