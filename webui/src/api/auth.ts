import { request } from '../utils/request';
import type { LoginRequest, TokenInfo, UserInfo, Result } from '../models';

// 认证API
export const authApi = {
  // 登录
  login: (data: LoginRequest) => {
    return request.post<Result<TokenInfo>>('/auth/login', data);
  },

  // 登出
  logout: () => {
    return request.post<Result<null>>('/auth/logout');
  },

  // 获取用户信息
  getUserInfo: () => {
    return request.get<Result<UserInfo>>('/auth/userInfo');
  },

  // 刷新token
  refreshToken: (refreshToken: string) => {
    return request.post<Result<TokenInfo>>('/auth/refresh', { refreshToken });
  },
};
