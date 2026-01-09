import {request} from '../utils/request';
import type {LoginRequest, Result, TokenInfo, UserInfo, UserInfoRegister} from '../models';

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

    // 注册
    register: (data: UserInfoRegister) => {
        return request.post<Result<string>>('/register/myself', data);
    },

    // 获取用户信息
    getUserInfo: () => {
        return request.get<Result<UserInfo>>('/user/info');
    },

};
