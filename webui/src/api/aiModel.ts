import {request} from '../utils/request';
import type {AIModelConfig, ModelType, Result} from '../models';

// AI模型API
export const aiModelApi = {
    // 获取模型列表
    list: (type: ModelType) => {
        return request.get<Result<AIModelConfig[]>>(`/ai/models/${type}`);
    },

    // 添加模型
    add: (data: AIModelConfig) => {
        return request.post<Result<AIModelConfig>>('/ai/models', data);
    },

    // 更新模型
    update: (id: string, data: AIModelConfig) => {
        return request.put<Result<AIModelConfig>>(`/ai/models/${id}`, data);
    },

    // 删除模型
    delete: (id: string) => {
        return request.delete<Result<null>>(`/ai/models/${id}`);
    },

    // 设置当前激活的模型
    setActive: (id: string) => {
        return request.put<Result<null>>(`/ai/models/${id}/active`);
    },
};
