/**
 * 图书相关实体类
 */

// 图书摘要
export class BookSummary {
  title: string;
  genre: string;
  themes: string[];
  tone: string;
  keyElements: string[];

  constructor(data: Partial<BookSummary> = {}) {
    this.title = data.title || '';
    this.genre = data.genre || '';
    this.themes = data.themes || [];
    this.tone = data.tone || '';
    this.keyElements = data.keyElements || [];
  }
}

// 书籍分析结果（平台共享数据）
export class BookAnalysis {
  id: string;
  title: string;
  genre: string;
  themes: string[];
  tone: string;
  keyElements: string[];
  posterUrl: string;
  recommendation: string;
  createTime?: string;
  createUser?: string;

  constructor(data: Partial<BookAnalysis> = {}) {
    this.id = data.id || '';
    this.title = data.title || '';
    this.genre = data.genre || '';
    this.themes = data.themes || [];
    this.tone = data.tone || '';
    this.keyElements = data.keyElements || [];
    this.posterUrl = data.posterUrl || '';
    this.recommendation = data.recommendation || '';
    this.createTime = data.createTime;
    this.createUser = data.createUser;
  }
}

// 用户兴趣关联（用户与书籍分析的关联）
export class UserInterest {
  id: string;
  userId: string;
  bookAnalyseId: string;
  interested?: boolean;
  reason?: string;
  interestSummary?: string;
  createTime?: string;

  constructor(data: Partial<UserInterest> = {}) {
    this.id = data.id || '';
    this.userId = data.userId || '';
    this.bookAnalyseId = data.bookAnalyseId || '';
    this.interested = data.interested;
    this.reason = data.reason;
    this.interestSummary = data.interestSummary;
    this.createTime = data.createTime;
  }
}

// 用户兴趣详情（包含书籍分析数据）
export class UserInterestDetail {
  userInterest: UserInterest;
  bookAnalysis: BookAnalysis;
  showPoster: boolean;

  constructor(data: Partial<UserInterestDetail> = {}) {
    this.userInterest = data.userInterest ? new UserInterest(data.userInterest) : new UserInterest();
    this.bookAnalysis = data.bookAnalysis ? new BookAnalysis(data.bookAnalysis) : new BookAnalysis();
    this.showPoster = data.showPoster ?? true;
  }
}

// 书籍推荐项
export interface BookRecommendItem {
  id: string;
  title: string;
}

// 反馈历史
export class FeedbackHistory {
  id: string;
  title: string;
  interested: boolean;
  reason?: string;
  timestamp: string;

  constructor(data: Partial<FeedbackHistory> = {}) {
    this.id = data.id || '';
    this.title = data.title || '';
    this.interested = data.interested ?? false;
    this.reason = data.reason;
    this.timestamp = data.timestamp || new Date().toISOString();
  }
}

// 分析历史（包含完整分析数据）
export class AnalysisHistory {
  id: string;
  title: string;
  interested: boolean;
  analysisData: BookAnalysis;
  createTime: string;

  constructor(data: Partial<AnalysisHistory> = {}) {
    this.id = data.id || '';
    this.title = data.title || '';
    this.interested = data.interested ?? false;
    this.analysisData = data.analysisData ? new BookAnalysis(data.analysisData) : new BookAnalysis();
    this.createTime = data.createTime || new Date().toISOString();
  }
}


// 年度报告
export class AnnualReport {
  year: number;
  totalBooks: number;
  interestedCount: number;
  topGenres: { genre: string; count: number }[];
  topThemes: { theme: string; count: number }[];
  monthlyTrend: { month: number; count: number }[];
  highlights: string[];

  constructor(data: Partial<AnnualReport> = {}) {
    this.year = data.year || new Date().getFullYear();
    this.totalBooks = data.totalBooks || 0;
    this.interestedCount = data.interestedCount || 0;
    this.topGenres = data.topGenres || [];
    this.topThemes = data.topThemes || [];
    this.monthlyTrend = data.monthlyTrend || [];
    this.highlights = data.highlights || [];
  }
}

// 用户偏好分析
export class UserPreference {
  summary: string;
  readingReport: {
    totalBooks: number;
    interestedBooks: number;
    favoriteGenres: string[];
    favoriteThemes: string[];
    readingTrend: string;
  };
  annualReport?: AnnualReport;

  constructor(data: Partial<UserPreference> = {}) {
    this.summary = data.summary || '';
    this.readingReport = data.readingReport || {
      totalBooks: 0,
      interestedBooks: 0,
      favoriteGenres: [],
      favoriteThemes: [],
      readingTrend: '',
    };
    this.annualReport = data.annualReport ? new AnnualReport(data.annualReport) : undefined;
  }
}
