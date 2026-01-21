import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { oauthApi } from '../api/oauth';
import { authApi } from '../api/auth';
import { toast } from '../components/ToastContainer';
import Logo from '../components/Logo';

/**
 * OAuth2 回调处理页面
 * 处理第三方平台的授权回调
 */
const OAuthCallbackPage: React.FC = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        const handleCallback = async () => {
            // 从 URL 获取参数
            const code = searchParams.get('code');
            const state = searchParams.get('state');

            // 从 localStorage 获取之前保存的信息
            const savedState = localStorage.getItem('oauth_state');
            const providerType = localStorage.getItem('oauth_provider');
            const redirect = localStorage.getItem('oauth_redirect') || '/';

            // 验证参数
            if (!code) {
                toast.error('授权失败：缺少授权码');
                navigate('/login');
                return;
            }

            if (!providerType) {
                toast.error('授权失败：无法识别平台类型');
                navigate('/login');
                return;
            }

            // 简化的 state 验证（可选）
            // if (state && savedState && state !== savedState) {
            //   toast.error('授权失败：安全验证未通过');
            //   navigate('/login');
            //   return;
            // }

            try {
                // 调用后端处理回调
                const response = await oauthApi.handleCallback(providerType, code, state || undefined);

                if (response.success && response.data) {
                    // 保存 token
                    localStorage.setItem('token', response.data.token);

                    // 获取用户信息
                    try {
                        const userInfoRes = await authApi.getUserInfo();
                        if (userInfoRes.success) {
                            localStorage.setItem('userInfo', JSON.stringify(userInfoRes.data));
                        }
                    } catch (error) {
                        console.error('获取用户信息失败:', error);
                    }

                    // 清理 localStorage 中的临时数据
                    localStorage.removeItem('oauth_state');
                    localStorage.removeItem('oauth_provider');
                    localStorage.removeItem('oauth_redirect');

                    toast.success('登录成功');

                    // 跳转到之前保存的 redirect 页面
                    navigate(redirect);
                } else {
                    toast.error(response.message || '登录失败');
                    navigate('/login');
                }
            } catch (error: any) {
                console.error('OAuth2 回调处理失败:', error);
                toast.error('登录失败，请重试');
                navigate('/login');
            }
        };

        handleCallback();
    }, [searchParams, navigate]);

    return (
        <div className="min-h-screen bg-white flex items-center justify-center p-4">
            <div className="text-center">
                <Logo className="w-16 h-16 mx-auto mb-4" />
                <h2 className="text-xl font-semibold text-gray-900 mb-2">正在登录...</h2>
                <p className="text-gray-600 mb-4">请稍候，正在处理授权信息</p>
                <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto" />
            </div>
        </div>
    );
};

export default OAuthCallbackPage;
