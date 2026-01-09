import axios, {AxiosError, type AxiosInstance, type AxiosRequestConfig} from 'axios';
import {AuthErrorCode, type Result} from '../models';
import {toast} from '../components/ToastContainer';

// 创建axios实例
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 添加token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers['token'] = token;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一处理Result包裹的响应
instance.interceptors.response.use(
  (response) => {
    const data: Result = response.data;

    // 如果返回success为false，视为业务错误
    if (data.success === false) {
      handleBusinessError(data);
      return Promise.reject(data);
    }

    return response;
  },
  (error: AxiosError<Result>) => {
    if (error.response) {
      const { status, data } = error.response;

      // 处理HTTP状态码错误
      if (status === 401) {
        handleAuthError(data);
      } else if (status === 403) {
        handleForbiddenError(data);
      } else if (status === 402) {
        handleExpiredError(data);
      } else {
        handleOtherError(status, data);
      }
    } else {
      // 网络错误
      console.error('网络错误:', error.message);
      toast.error('网络连接失败，请检查网络');
    }

    return Promise.reject(error);
  }
);

// 处理401认证错误
function handleAuthError(data?: Result) {
  const errorCode = data?.message || '';

  // 根据错误类型处理
  if (errorCode.includes(AuthErrorCode.TOKEN_ERROR)) {
    console.error('Token校验失败');
  } else if (errorCode.includes(AuthErrorCode.REDIS_EXPIRED_USER)) {
    console.error('登录已失效');
  } else if (errorCode.includes(AuthErrorCode.REDIS_NO_USER)) {
    console.error('非法登录');
  }

  // 清除token并跳转登录
  clearAuthAndRedirect();
}

// 处理403权限错误
function handleForbiddenError(data?: Result) {
  const errorCode = data?.message || '';

  if (errorCode.includes(AuthErrorCode.UNAUTHENTICATED)) {
    toast.error('当前用户无访问权限');
  } else if (errorCode.includes(AuthErrorCode.UNAUTHENTICATED_PLATFORM)) {
    toast.error('非法令牌访问');
    clearAuthAndRedirect();
  } else {
    toast.error(data?.message || '无访问权限');
  }
}

// 处理402授权过期
function handleExpiredError(data?: Result) {
  toast.error(data?.message || '系统授权已过期');
}

// 处理其他错误
function handleOtherError(status: number, data?: Result) {
  const message = data?.message || `请求失败 (${status})`;
  console.error('请求错误:', message);
  toast.error(message);
}

// 处理业务错误
function handleBusinessError(data: Result) {
  console.error('业务错误:', data.message);

  // 特殊处理错误码 1001 - 书籍已分析
  if (data.code === 1001) {
    // 不显示 toast，由调用方处理跳转
    return;
  }

  toast.error(data.message || '操作失败');
}

// 清除认证信息并跳转登录
function clearAuthAndRedirect() {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');

  // 如果不在登录页则跳转
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

// 封装请求方法 - 直接返回完整的Result对象
export const request = {
  get: <T = Result>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return instance.get(url, config).then(res => res.data);
  },

  post: <T = Result>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    return instance.post(url, data, config).then(res => res.data);
  },

  put: <T = Result>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    return instance.put(url, data, config).then(res => res.data);
  },

  delete: <T = Result>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return instance.delete(url, config).then(res => res.data);
  },
};

export default instance;
