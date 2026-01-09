/**
 * 设置相关实体类
 */

// AI 分析模型配置
export class AIAnalysisModel {
  id: string;
  name: string;
  type: 'openai' | 'ollama' | 'deepseek' | 'custom';
  apiKey?: string;
  apiUrl?: string;
  model?: string;
  enabled: boolean;
  isActive?: boolean;

  constructor(data: Partial<AIAnalysisModel> = {}) {
    this.id = data.id || '';
    this.name = data.name || '';
    this.type = data.type || 'openai';
    this.apiKey = data.apiKey;
    this.apiUrl = data.apiUrl;
    this.model = data.model;
    this.enabled = data.enabled ?? true;
    this.isActive = data.isActive;
  }
}

// AI 生图模型配置
export class AIImageModel {
  id: string;
  name: string;
  type: 'stable-diffusion' | 'midjourney' | 'dall-e' | 'custom';
  apiKey?: string;
  apiUrl?: string;
  enabled: boolean;
  isActive?: boolean;

  constructor(data: Partial<AIImageModel> = {}) {
    this.id = data.id || '';
    this.name = data.name || '';
    this.type = data.type || 'stable-diffusion';
    this.apiKey = data.apiKey;
    this.apiUrl = data.apiUrl;
    this.enabled = data.enabled ?? true;
    this.isActive = data.isActive;
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
