import {request} from '../utils/request';
import type {OAuth2Provider, UserThirdPartyBinding} from "../models/OAuth2.ts";
import {type Result} from '../models';

/**
 * OAuth2 第三方登录 API
 */
export const oauthApi = {
  /**
   * 获取启用的第三方平台列表
   */
  getProviders: async () => {
    return request.get<Result<OAuth2Provider[]>>('/oauth/providers');
  },

  /**
   * 获取授权URL
   * @param providerType 平台类型（如 'github', 'linuxdo'）
   */
  getAuthorizeUrl: async (providerType: string) => {
    return request.get(`/oauth/authorize/${providerType}`);
  },

  /**
   * 处理OAuth2回调
   * @param providerType 平台类型
   * @param code 授权码
   * @param state 状态码
   */
  handleCallback: async (providerType: string, code: string, state?: string) => {
    const params: any = { code };
    if (state) {
      params.state = state;
    }
    return request.get(`/oauth/callback/${providerType}`, { params });
  },

  /**
   * 获取用户已绑定的第三方账户列表
   */
  getUserBindings: async () => {
    return request.get<Result<UserThirdPartyBinding[]>>('/user/third-party/bindings');
  },

  /**
   * 绑定第三方账户
   * @param data 绑定请求数据
   */
  bindAccount: async (data: { providerType: string; code: string; state?: string }) => {
    return request.post('/user/third-party/bind', data);
  },

  /**
   * 解绑第三方账户
   * @param providerType 平台类型
   */
  unbindAccount: async (providerType: string) => {
    return request.delete(`/user/third-party/unbind/${providerType}`);
  },
};
