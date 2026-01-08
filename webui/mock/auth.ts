/**
 * Mock接口 - 认证相关
 */
import {MockMethod} from 'vite-plugin-mock';
import {validateUser} from './data/user.data';
import type {Result, TokenInfo, UserInfo} from '../src/models';

export default [
  // 登录接口
  {
    url: '/api/auth/login',
    method: 'post',
    timeout: 1000,
    response: ({ body }: any): Result<TokenInfo> => {
      const { username, password } = body;
      const user = validateUser(username, password);

      if (user) {
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
          data: null as any,
          success: false,
        };
      }
    },
  },

  // 登出接口
  {
    url: '/api/auth/logout',
    method: 'post',
    timeout: 500,
    response: (): Result<null> => {
      return {
        code: 200,
        message: '登出成功',
        ts: Date.now(),
        data: null,
        success: true,
      };
    },
  },

  // 获取用户信息
  {
    url: '/api/auth/userInfo',
    method: 'get',
    timeout: 500,
    response: ({ headers }: any): Result<UserInfo> => {
      const token = headers.token;

      if (!token || !token.startsWith('mock_token_')) {
        return {
          code: 401,
          message: 'TOKEN_ERROR',
          ts: Date.now(),
          data: null as any,
          success: false,
        };
      }

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
  },

  // 刷新token
  {
    url: '/api/auth/refresh',
    method: 'post',
    timeout: 500,
    response: (): Result<TokenInfo> => {
      const newToken = 'mock_token_' + Date.now();
      return {
        code: 200,
        message: '刷新成功',
        ts: Date.now(),
        data: {
          token: newToken,
          expireTime: Date.now() + 24 * 60 * 60 * 1000,
        },
        success: true,
      };
    },
  },
] as MockMethod[];
