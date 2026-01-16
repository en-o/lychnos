/**
 * Mock接口 - AI模型相关
 *
 * ⚠️ 注意：Mock接口已停止维护，存在数据不一致问题
 * 请使用真实后端接口进行开发和测试
 */
import {MockMethod} from 'vite-plugin-mock';
import {addModel, deleteModel, getModelsByType, setActiveModel, updateModel} from './data/aiModel.data';
import type {AIModelConfig, Result} from '../src/models';

export default [
  // 获取模型列表
  {
    url: '/api/ai/models/:type',
    method: 'get',
    timeout: 500,
    response: (req: any): Result<AIModelConfig[]> => {
      // 从 URL 中提取 type 参数
      const urlParts = req.url.split('/');
      const type = urlParts[urlParts.length - 1].split('?')[0];

      if (!type || (type !== 'TEXT' && type !== 'IMAGE')) {
        return {
          code: 400,
          message: '参数错误',
          ts: Date.now(),
          data: null as any,
          success: false,
        };
      }

      const models = getModelsByType(type as 'TEXT' | 'IMAGE');

      return {
        code: 200,
        message: 'success',
        ts: Date.now(),
        data: models,
        success: true,
      };
    },
  },

  // 添加模型
  {
    url: '/api/ai/models',
    method: 'post',
    timeout: 800,
    response: ({body}: any): Result<AIModelConfig> => {
      const model = addModel(body);

      return {
        code: 200,
        message: '添加成功',
        ts: Date.now(),
        data: model,
        success: true,
      };
    },
  },

  // 更新模型
  {
    url: '/api/ai/models/:id',
    method: 'put',
    timeout: 800,
    response: (req: any): Result<AIModelConfig> => {
      // 从 URL 中提取 id 参数
      const urlParts = req.url.split('/');
      const id = urlParts[urlParts.length - 1].split('?')[0];

      const updated = updateModel(id, req.body);

      if (updated) {
        return {
          code: 200,
          message: '更新成功',
          ts: Date.now(),
          data: updated,
          success: true,
        };
      } else {
        return {
          code: 404,
          message: '模型不存在',
          ts: Date.now(),
          data: null as any,
          success: false,
        };
      }
    },
  },

  // 删除模型
  {
    url: '/api/ai/models/:id',
    method: 'delete',
    timeout: 500,
    response: (req: any): Result<null> => {
      // 从 URL 中提取 id 参数
      const urlParts = req.url.split('/');
      const id = urlParts[urlParts.length - 1].split('?')[0];

      const success = deleteModel(id);

      if (success) {
        return {
          code: 200,
          message: '删除成功',
          ts: Date.now(),
          data: null,
          success: true,
        };
      } else {
        return {
          code: 404,
          message: '模型不存在',
          ts: Date.now(),
          data: null,
          success: false,
        };
      }
    },
  },

  // 设置激活的模型
  {
    url: '/api/ai/models/:id/active',
    method: 'put',
    timeout: 500,
    response: (req: any): Result<null> => {
      // 从 URL 中提取 id 参数
      const urlParts = req.url.split('/');
      const id = urlParts[urlParts.length - 2]; // active 前面是 id

      const success = setActiveModel(id);

      if (success) {
        return {
          code: 200,
          message: '设置成功',
          ts: Date.now(),
          data: null,
          success: true,
        };
      } else {
        return {
          code: 404,
          message: '模型不存在',
          ts: Date.now(),
          data: null,
          success: false,
        };
      }
    },
  },
] as MockMethod[];
