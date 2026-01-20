import {request} from '../utils/request';
import {
  type AnalysisHistory,
  type BookAnalysis,
  type BookExtract,
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

  // 提取书籍信息（从用户输入中提取书名和作者）
  extractBooks: (input: string) => {
    return request.post<Result<BookExtract[]>>('/book/extract', { input });
  },

  // 查询书籍分析结果（已登录用户检查是否已反馈，未登录用户只能查看推荐书籍）
  queryBookAnalysis: (title: string) => {
    return request.get<Result<BookAnalysis | null>>(`/book/query/${encodeURIComponent(title)}`);
  },

  // 分析图书（需要较长时间：AI分析+图片生成）
  analyzeBook: (bookInfo: { title: string; author?: string }) => {
    return request.put<Result<BookAnalysis>>(
      '/book/analyze',
      bookInfo,
      { timeout: 120000 } // 2分钟超时
    );
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
