import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import Logo from '../components/Logo';
import { toast } from '../components/ToastContainer';
import { authApi } from '../api/auth';
import { oauthApi } from '../api/oauth';
import type { OAuth2Provider } from '../models/OAuth2';
import { generateRandomString } from '../utils/random';

const RegisterPage: React.FC = () => {
  // ...
  // 处理第三方登录
  const handleOAuthLogin = async (providerType: string) => {
    try {
      // 生成随机 state 并保存到 localStorage
      const state = generateRandomString();
      localStorage.setItem('oauth_state', state);
      localStorage.setItem('oauth_provider', providerType);
      localStorage.setItem('oauth_action', 'login'); // 明确标记为登录动作

      // 保存 redirect 参数（注册页通常没有 redirect，默认为 /）
      localStorage.setItem('oauth_redirect', '/');

      // 构造回调地址
      const redirectUri = window.location.origin + '/oauth/callback';

      // 获取授权 URL
      const response = await oauthApi.getAuthorizeUrl(providerType, state, redirectUri);
      if (response.success && response.data) {
        // 跳转到第三方授权页面
        window.location.href = response.data;
      } else {
        toast.error('生成授权链接失败');
      }
    } catch (error) {
      console.error('第三方登录失败:', error);
      toast.error('第三方登录失败，请稍后重试');
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.loginName) {
      newErrors.loginName = '请输入用户名';
    } else if (formData.loginName.length < 3) {
      newErrors.loginName = '用户名至少3个字符';
    }

    if (!formData.password) {
      newErrors.password = '请输入密码';
    } else if (formData.password.length < 3) {
      newErrors.password = '密码至少6个字符';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '请输入有效的邮箱地址';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      // TODO: 调用真实的注册API
      await authApi.register(formData);
      toast.success('注册成功!请登录');
      navigate('/login');
    } catch (error: any) {
      console.error('注册失败:', error);
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
          <p className="text-gray-600">创建新账号</p>
        </div>

        {/* 注册表单 */}
        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              用户名 *
            </label>
            <input
              type="text"
              value={formData.loginName}
              onChange={(e) => {
                setFormData({ ...formData, loginName: e.target.value });
                setErrors({ ...errors, loginName: '' });
              }}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入用户名(至少3个字符)"
              disabled={loading}
            />
            {errors.loginName && (
              <p className="text-sm text-red-600 mt-1">{errors.loginName}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              昵称
            </label>
            <input
              type="text"
              value={formData.nickname}
              onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入昵称(可选)"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              邮箱
            </label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => {
                setFormData({ ...formData, email: e.target.value });
                setErrors({ ...errors, email: '' });
              }}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入邮箱(可选)"
              disabled={loading}
            />
            {errors.email && (
              <p className="text-sm text-red-600 mt-1">{errors.email}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              密码 *
            </label>
            <div className="relative">
              <input
                type={showPasswords.password ? 'text' : 'password'}
                value={formData.password}
                onChange={(e) => {
                  setFormData({ ...formData, password: e.target.value });
                  setErrors({ ...errors, password: '' });
                }}
                className="w-full px-4 py-2.5 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="请输入密码(至少3个字符)"
                disabled={loading}
              />
              <button
                type="button"
                onClick={() =>
                  setShowPasswords({ ...showPasswords, password: !showPasswords.password })
                }
                className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.password ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
            {errors.password && (
              <p className="text-sm text-red-600 mt-1">{errors.password}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              确认密码 *
            </label>
            <div className="relative">
              <input
                type={showPasswords.confirm ? 'text' : 'password'}
                value={formData.confirmPassword}
                onChange={(e) => {
                  setFormData({ ...formData, confirmPassword: e.target.value });
                  setErrors({ ...errors, confirmPassword: '' });
                }}
                className="w-full px-4 py-2.5 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="请再次输入密码"
                disabled={loading}
              />
              <button
                type="button"
                onClick={() =>
                  setShowPasswords({ ...showPasswords, confirm: !showPasswords.confirm })
                }
                className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.confirm ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-sm text-red-600 mt-1">{errors.confirmPassword}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-medium"
          >
            {loading ? (
              <div className="flex items-center justify-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                注册中...
              </div>
            ) : (
              '注册'
            )}
          </button>
        </form>

        {/* 第三方登录 */}
        {providers.length > 0 && (
          <>
            {/* 分隔线 */}
            <div className="flex items-center my-6">
              <div className="flex-1 border-t border-gray-300"></div>
              <span className="px-4 text-sm text-gray-500">或使用第三方账号注册/登录</span>
              <div className="flex-1 border-t border-gray-300"></div>
            </div>

            {/* 第三方登录按钮 */}
            <div className="grid grid-cols-2 gap-3">
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

        {/* 登录链接 */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            已有账号?{' '}
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">
              立即登录
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
