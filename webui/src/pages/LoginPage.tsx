import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router-dom';
import {authApi} from '../api/auth';
import {oauthApi} from '../api/oauth';
import type {OAuth2Provider} from '../models/OAuth2';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirect = searchParams.get('redirect') || '/';

  const [loginName, setLoginName] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [providers, setProviders] = useState<OAuth2Provider[]>([]);

  // 加载第三方登录平台列表
  useEffect(() => {
    const loadProviders = async () => {
      try {
        const response = await oauthApi.getProviders();
        if (response.success && response.data) {
          setProviders(response.data);
        }
      } catch (error) {
        console.error('加载第三方登录平台失败:', error);
      }
    };
    loadProviders();
  }, []);

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

  // 处理第三方登录
  const handleOAuthLogin = async (providerType: string) => {
    try {
      // 1. 获取授权 URL (不传参数，由后端生成 state)
      const response = await oauthApi.getAuthorizeUrl(providerType);

      if (response.success && response.data) {
        const authorizeUrl = response.data;

        // 2. 解析 URL 获取后端生成的 state
        const urlObj = new URL(authorizeUrl);
        const state = urlObj.searchParams.get('state');

        if (state) {
          // 3. 保存 state 到 localStorage 用于回调验证
          localStorage.setItem('oauth_state', state);
          localStorage.setItem('oauth_provider', providerType);
          localStorage.setItem('oauth_action', 'login');
          localStorage.setItem('oauth_redirect', redirect);

          // 4. 跳转到第三方授权页面
          window.location.href = authorizeUrl;
        } else {
          toast.error('授权链接异常：缺少state');
        }
      } else {
        toast.error('生成授权链接失败');
      }
    } catch (error) {
      console.error('第三方登录失败:', error);
      toast.error('第三方登录失败，请稍后重试');
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

        {/* 第三方登录 */}
        {providers.length > 0 && (
          <>
            {/* 分隔线 */}
            <div className="flex items-center my-6">
              <div className="flex-1 border-t border-gray-300"></div>
              <span className="px-4 text-sm text-gray-500">或使用第三方账号登录</span>
              <div className="flex-1 border-t border-gray-300"></div>
            </div>

            {/* 第三方登录按钮 */}
            <div className={`grid gap-3 ${providers.length > 1 ? 'grid-cols-2' : 'grid-cols-1'}`}>
              {providers.map(provider => (
                <button
                  key={provider.type}
                  onClick={() => handleOAuthLogin(provider.type)}
                  className="flex items-center justify-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  {provider.iconUrl && (
                    <img src={provider.iconUrl} alt={provider.name} className="w-5 h-5" />
                  )}
                  <span className="text-sm">{provider.name}</span>
                </button>
              ))}
            </div>
          </>
        )}

        {/* 注册链接 */}
        <div className="mt-6 text-center">
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
