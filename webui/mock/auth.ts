/**
 * Mock接口 - 认证相关
 *
 * ⚠️ 注意：Mock接口已停止维护，存在数据不一致问题
 * 请使用真实后端接口进行开发和测试
 */
import {MockMethod} from 'vite-plugin-mock';
import {validateUser} from './data/user.data';
import type {Result, TokenInfo, UserInfo} from '../src/models';

export default [
  // 登录接口
  {
    url: '/api/login',
    method: 'post',
    timeout: 1000,
    response: ({ body }: any): Result<TokenInfo> => {
      const { loginName, password } = body;
      const user = validateUser(loginName, password);

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
    url: '/api/logout',
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
    url: '/api/user/info',
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
          loginName: 'admin',
          nickname: '管理员',
          email: 'admin@admin.com',
          id: '1',
        },
        success: true,
      };
    },
  },
] as MockMethod[];
