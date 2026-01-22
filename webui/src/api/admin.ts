import {request} from '../utils/request';
import type {Result} from '../models';

// OAuth配置详情
export interface OAuthConfigDetail {
    id: number;
    providerType: string;
    providerName: string;
    clientId: string;
    authorizeUrl: string;
    tokenUrl: string;
    userInfoUrl: string;
    scope: string;
    iconUrl: string;
    sortOrder: number;
    enabled: boolean;
    webCallbackUrl: string;
    createTime: string;
    updateTime: string;
}

// OAuth配置更新DTO
export interface OAuthConfigUpdate {
    id: number;
    providerType?: string;
    clientId?: string;
    clientSecret?: string;
    authorizeUrl?: string;
    tokenUrl?: string;
    userInfoUrl?: string;
    scope?: string;
    iconUrl?: string;
    sortOrder?: number;
    webCallbackUrl?: string;
}

// 用户详情
export interface UserDetail {
    id: number;
    loginName: string;
    nickname: string;
    email: string;
    roles: string[];
    createTime: string;
    updateTime: string;
}

// 第三方绑定信息（管理员视图，仅包含非敏感字段）
export interface ThirdPartyBind {
    userId: number;
    providerType: string;
    nickname: string;
    avatarUrl: string;
    createTime: string;
}

// AI模型详情
export interface AIModelDetail {
    id: number;
    userId: number;
    loginName: string;
    name: string;
    model: string;
    factory: string;
    apiKey: string;
    apiUrl: string;
    enabled: boolean;
    type: string;
    share: number;
    createTime: string;
    updateTime: string;
}

// 分页结果
export interface PageResult<T> {
    page: number;
    size: number;
    totalPages: number;
    total: number;
    rows: T[];
}

// 分页请求参数
export interface PageRequest {
    page?: {
        pageNum?: number;
        pageSize?: number;
    };
    loginName?: string;
}

// 管理员API
export const adminApi = {
    // OAuth配置管理
    oauth: {
        // 获取所有OAuth配置列表
        list: () => {
            return request.get<Result<OAuthConfigDetail[]>>('/sys-manage/oauth-config/list');
        },

        // 新增OAuth配置
        create: (data: OAuthConfigUpdate) => {
            return request.post<Result<void>>('/sys-manage/oauth-config/create', data);
        },

        // 更新OAuth配置
        update: (data: OAuthConfigUpdate) => {
            return request.put<Result<void>>('/sys-manage/oauth-config/update', data);
        },

        // 启用/停用OAuth配置
        toggle: (id: number) => {
            return request.put<Result<void>>(`/sys-manage/oauth-config/toggle/${id}`);
        },

        // 更新OAuth配置排序
        updateSort: (id: number, sortOrder: number) => {
            return request.put<Result<void>>(`/sys-manage/oauth-config/update-sort/${id}/${sortOrder}`);
        },

        // 删除OAuth配置
        delete: (id: number) => {
            return request.delete<Result<void>>(`/sys-manage/oauth-config/delete/${id}`);
        },
    },

    // 用户管理
    user: {
        // 获取所有用户列表
        list: () => {
            return request.get<Result<UserDetail[]>>('/sys-manage/user/list');
        },

        // 获取用户详情
        detail: (id: number) => {
            return request.get<Result<UserDetail>>(`/sys-manage/user/detail/${id}`);
        },

        // 获取用户的第三方绑定列表
        thirdPartyBindings: (userId: number) => {
            return request.get<Result<ThirdPartyBind[]>>(`/sys-manage/user/third-party-bindings/${userId}`);
        },
    },

    // AI模型管理
    aiModel: {
        // 获取所有AI模型列表（分页）
        list: (params: PageRequest) => {
            return request.post<Result<PageResult<AIModelDetail>>>('/sys-manage/ai-model/list', params);
        },

        // 设置模型为官方
        setOfficial: (id: number) => {
            return request.put<Result<void>>(`/sys-manage/ai-model/set-official/${id}`);
        },

        // 设置模型为私人
        setPrivate: (id: number) => {
            return request.put<Result<void>>(`/sys-manage/ai-model/set-private/${id}`);
        },
    },
};
