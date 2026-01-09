/**
 * 认证相关实体类
 */

// Token信息
export interface TokenInfo {
  token: string;
  refreshToken?: string;
  expireTime?: number;
}

// 登录请求
export interface LoginRequest {
  loginName: string;
  password: string;
}

// 用户信息
export interface UserInfo {
  loginName: string;
  nickname: string;
  id: string;
  email?: string;
  createTime?: string;
}


// 注册用户信息
export interface UserInfoRegister {
  loginName: string;
  nickname: string;
  password: string;
  email?: string;
}

// 错误码常量
export const AuthErrorCode = {
  TOKEN_ERROR: 'TOKEN_ERROR',
  REDIS_EXPIRED_USER: 'REDIS_EXPIRED_USER',
  REDIS_NO_USER: 'REDIS_NO_USER',
  SYS_AUTHORIZED_PAST: 'SYS_AUTHORIZED_PAST',
  UNAUTHENTICATED: 'UNAUTHENTICATED',
  UNAUTHENTICATED_PLATFORM: 'UNAUTHENTICATED_PLATFORM',
} as const;
