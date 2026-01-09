import {request} from '../utils/request';
import {
  type AnalysisHistory,
  type BookAnalysis,
  type BookRecommendItem,
  type PageResult,
  type Result,
  type UserPreference,
  type UserInterest,
} from '../models';
import type {InterestFeedback} from "../models/interest.model.ts";

// 图书API
export const bookApi = {
  // 获取快速推荐书籍列表
  getQuickBooks: () => {
    return request.get<Result<BookRecommendItem[]>>('/book/recommend');
  },

  // 分析图书
  analyzeBook: (title: string) => {

    return request.put<Result<BookAnalysis>>(`/book/analyze/${encodeURIComponent(title)}`);
  },

  // 提交用户反馈（创建用户兴趣）
  submitFeedback: (data: InterestFeedback) => {
    return request.post<Result<UserInterest>>('/user/interest', data);
  },

  // 获取反馈历史
  getFeedbackHistory: () => {
    return request.get<Result<AnalysisHistory[]>>('/book/feedback/history');
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
