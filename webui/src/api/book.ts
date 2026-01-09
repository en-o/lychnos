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

  // 提交用户分析
  submitFeedback: (data: InterestFeedback) => {
    return request.post<Result<UserInterest>>('/user/interest', data);
  },

  // 获取最近分析（返回完整分析历史）
  getFeedbackHistory: () => {
    return request.get<Result<AnalysisHistory[]>>('/user/recent/analysis');
  },

  // 获取分析历史（分页）
  getAnalysisHistory: (page: number, pageSize: number, bookTitle?: string) => {
    return request.post<Result<PageResult<AnalysisHistory>>>('/user/history/analysis', {
      bookTitle,
      page: {
        pageIndex: page,
        pageSize: pageSize
      }
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
