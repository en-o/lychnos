import {request} from '../utils/request';
import type {LoginRequest, Result, TokenInfo, UserInfo} from '../models';

// 认证API
export const authApi = {
  // 登录
  login: (data: LoginRequest) => {
    return request.post<Result<TokenInfo>>('/login', data);
  },

  // 登出
  logout: () => {
    return request.post<Result<string>>('/logout');
  },

  // 获取用户信息
  getUserInfo: () => {
    return request.get<Result<UserInfo>>('/user/info');
  },

};
