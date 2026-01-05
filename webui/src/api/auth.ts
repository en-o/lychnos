import { request } from '../utils/request';
import type { LoginRequest, TokenInfo, UserInfo } from '../types/auth';

// 登录API
export const authApi = {
  // 登录
  login: (data: LoginRequest) => {
    return request.post<TokenInfo>('/auth/login', data);
  },

  // 登出
  logout: () => {
    return request.post('/auth/logout');
  },

  // 获取用户信息
  getUserInfo: () => {
    return request.get<UserInfo>('/auth/userInfo');
  },

  // 刷新token
  refreshToken: (refreshToken: string) => {
    return request.post<TokenInfo>('/auth/refresh', { refreshToken });
  },
};

// Mock数据(开发阶段使用)
export const mockAuthApi = {
  login: async (data: LoginRequest) => {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 简单的mock验证
    if (data.username === 'admin' && data.password === 'admin') {
      const mockToken = 'mock_token_' + Date.now();
      return {
        code: 200,
        message: '登录成功',
        ts: Date.now(),
        data: {
          token: mockToken,
          expireTime: Date.now() + 24 * 60 * 60 * 1000,
        },
        success: true,
      };
    } else {
      return {
        code: 401,
        message: '用户名或密码错误',
        ts: Date.now(),
        data: null,
        success: false,
      };
    }
  },

  getUserInfo: async () => {
    await new Promise(resolve => setTimeout(resolve, 500));
    return {
      code: 200,
      message: 'success',
      ts: Date.now(),
      data: {
        username: 'admin',
        nickname: '管理员',
        userId: '1',
      },
      success: true,
    };
  },
};
