import React, {useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router-dom';
import {authApi} from '../api/auth';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirect = searchParams.get('redirect') || '/';

  const [loginName, setLoginName] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!loginName || !password) {
      toast.warning('请输入用户名和密码');
      return;
    }

    setLoading(true);

    try {
      const response = await authApi.login({ loginName, password });

      if (response.success && response.data) {
        // 保存token
        localStorage.setItem('token', response.data.token);

        // 获取用户信息
        const userInfoRes = await authApi.getUserInfo();
        if (userInfoRes.success) {
          localStorage.setItem('userInfo', JSON.stringify(userInfoRes.data));
        }

        // 跳转到redirect指定的页面
        navigate(redirect);
      } else {
        toast.error(response.message);
      }
    } catch (error: any) {
      console.error('登录失败:', error);
      // 错误提示已在request拦截器中统一处理，不需要重复提示
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo和标题 */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-4">
            <Logo className="w-8 h-8" />
            <h1 className="text-2xl font-semibold text-gray-900">书灯</h1>
          </div>
          <p className="text-gray-600">欢迎回来</p>
        </div>

        {/* 登录表单 */}
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              用户名
            </label>
            <input
              type="text"
              value={loginName}
              onChange={(e) => setLoginName(e.target.value)}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入用户名"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              密码
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入密码"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-medium"
          >
            {loading ? (
              <div className="flex items-center justify-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                登录中...
              </div>
            ) : (
              '登录'
            )}
          </button>
        </form>

        {/* 提示信息 */}
        {/*<div className="mt-6 p-3 bg-gray-50 rounded-lg border border-gray-200">*/}
        {/*  <p className="text-xs text-gray-600 mb-1">演示账号:</p>*/}
        {/*  <p className="text-xs text-gray-700">用户名: admin / 密码: admin</p>*/}
        {/*</div>*/}

        {/* 注册链接 */}
        <div className="mt-4 text-center">
          <p className="text-sm text-gray-600">
            还没有账号?{' '}
            <Link to="/register" className="text-blue-600 hover:text-blue-700 font-medium">
              立即注册
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
