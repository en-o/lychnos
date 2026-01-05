// JWT响应格式
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  ts: number;
  data: T;
  success: boolean;
  traceId?: string;
}

// Token信息
export interface TokenInfo {
  token: string;
  refreshToken?: string;
  expireTime?: number;
}

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 用户信息
export interface UserInfo {
  username: string;
  nickname?: string;
  userId?: string;
  [key: string]: any;
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
