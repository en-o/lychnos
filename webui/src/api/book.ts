import { request } from '../utils/request';
import type {
  BookAnalysis,
  FeedbackHistory,
  AnalysisHistory,
  PaginatedResponse,
  UserPreference,
} from '../models';

// 图书API
export const bookApi = {
  // 获取快速推荐书籍列表
  getQuickBooks: () => {
    return request.get<string[]>('/book/quick');
  },

  // 分析图书
  analyzeBook: (title: string) => {
    return request.post<BookAnalysis>('/book/analyze', { title });
  },

  // 提交反馈
  submitFeedback: (bookId: number, interested: boolean, reason?: string) => {
    return request.post('/book/feedback', { bookId, interested, reason });
  },

  // 获取反馈历史
  getFeedbackHistory: () => {
    return request.get<FeedbackHistory[]>('/book/feedback/history');
  },

  // 获取分析历史（分页）
  getAnalysisHistory: (page: number, pageSize: number) => {
    return request.get<PaginatedResponse<AnalysisHistory>>('/book/history', {
      params: { page, pageSize }
    });
  },

  // 获取用户偏好
  getUserPreference: () => {
    return request.get<UserPreference>('/user/preference');
  },

  // 下载年度报告
  downloadAnnualReport: (year: number) => {
    return request.get(`/user/report/${year}/download`, { responseType: 'blob' });
  },
};
