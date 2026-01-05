// AI 分析模型配置
export interface AIAnalysisModel {
  id: string;
  name: string;
  type: 'openai' | 'ollama' | 'deepseek' | 'custom';
  apiKey?: string;
  apiUrl?: string;
  model?: string;
  enabled: boolean;
  isActive?: boolean;
}

// AI 生图模型配置
export interface AIImageModel {
  id: string;
  name: string;
  type: 'stable-diffusion' | 'midjourney' | 'dall-e' | 'custom';
  apiKey?: string;
  apiUrl?: string;
  enabled: boolean;
  isActive?: boolean;
}

// 用户资料
export interface UserProfile {
  username: string;
  nickname?: string;
  email?: string;
  avatar?: string;
  createdAt?: string;
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
