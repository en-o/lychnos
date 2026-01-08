import {request} from '../utils/request';
import type {AnalysisHistory, BookAnalysis, FeedbackHistory, PageResult, Result, UserPreference,} from '../models';

// 图书API
export const bookApi = {
  // 获取快速推荐书籍列表
  getQuickBooks: () => {
    return request.get<Result<string[]>>('/book/quick');
  },

  // 分析图书
  analyzeBook: (title: string) => {
    return request.post<Result<BookAnalysis>>('/book/analyze', { title });
  },

  // 提交反馈
  submitFeedback: (bookId: number, interested: boolean, reason?: string) => {
    return request.post<Result<null>>('/book/feedback', { bookId, interested, reason });
  },

  // 获取反馈历史
  getFeedbackHistory: () => {
    return request.get<Result<FeedbackHistory[]>>('/book/feedback/history');
  },

  // 获取分析历史（分页）
  getAnalysisHistory: (page: number, pageSize: number) => {
    return request.get<Result<PageResult<AnalysisHistory>>>('/book/history', {
      params: { page, pageSize }
    });
  },

  // 获取用户偏好
  getUserPreference: () => {
    return request.get<Result<UserPreference>>('/user/preference');
  },

  // 下载年度报告
  downloadAnnualReport: (year: number) => {
    return request.get(`/user/report/${year}/download`, { responseType: 'blob' });
  },
};
