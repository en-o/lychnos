/**
 * 设置相关实体类
 */

// 模型类型枚举
export type ModelType = 'TEXT' | 'IMAGE';

// AI 模型配置
export class AIModelConfig {
  id?: string;
  userId?: string;
  name: string;
  model: string;
  factory: string; // openai | ollama | deepseek | azure | anthropic | qwen | baidu | stable-diffusion | midjourney | dall-e | custom
  apiKey?: string;
  apiUrl: string;
  enabled: boolean;
  type: ModelType;

  constructor(data: Partial<AIModelConfig> = {}) {
    this.id = data.id;
    this.userId = data.userId;
    this.name = data.name || '';
    this.model = data.model || '';
    this.factory = data.factory || 'openai';
    this.apiKey = data.apiKey;
    this.apiUrl = data.apiUrl || '';
    this.enabled = data.enabled ?? true;
    this.type = data.type || 'TEXT';
  }
}

// 用户资料
export class UserProfile {
  loginName: string;
  nickname?: string;
  email?: string;
  avatar?: string;
  createTime?: string;

  constructor(data: Partial<UserProfile> = {}) {
    this.loginName = data.loginName || '';
    this.nickname = data.nickname;
    this.email = data.email;
    this.avatar = data.avatar;
    this.createTime = data.createTime;
  }
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
