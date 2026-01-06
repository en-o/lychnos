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
  triggerWarnings: string[];

  constructor(data: Partial<BookSummary> = {}) {
    this.title = data.title || '';
    this.genre = data.genre || '';
    this.themes = data.themes || [];
    this.tone = data.tone || '';
    this.keyElements = data.keyElements || [];
    this.triggerWarnings = data.triggerWarnings || [];
  }
}

// 图书分析结果
export class BookAnalysis {
  bookId: number;
  summary: BookSummary;
  posterUrl: string;
  recommendation: string;
  showPoster: boolean;

  constructor(data: Partial<BookAnalysis> = {}) {
    this.bookId = data.bookId || 0;
    this.summary = data.summary ? new BookSummary(data.summary) : new BookSummary();
    this.posterUrl = data.posterUrl || '';
    this.recommendation = data.recommendation || '';
    this.showPoster = data.showPoster ?? true;
  }
}

// 反馈历史
export class FeedbackHistory {
  bookId: number;
  title: string;
  interested: boolean;
  reason?: string;
  timestamp: string;

  constructor(data: Partial<FeedbackHistory> = {}) {
    this.bookId = data.bookId || 0;
    this.title = data.title || '';
    this.interested = data.interested ?? false;
    this.reason = data.reason;
    this.timestamp = data.timestamp || new Date().toISOString();
  }
}

// 分析历史（包含完整分析数据）
export class AnalysisHistory {
  id: string;
  bookId: number;
  title: string;
  interested: boolean;
  analysisData: BookAnalysis;
  createdAt: string;

  constructor(data: Partial<AnalysisHistory> = {}) {
    this.id = data.id || '';
    this.bookId = data.bookId || 0;
    this.title = data.title || '';
    this.interested = data.interested ?? false;
    this.analysisData = data.analysisData ? new BookAnalysis(data.analysisData) : new BookAnalysis();
    this.createdAt = data.createdAt || new Date().toISOString();
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
