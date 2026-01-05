import { request } from '../utils/request';

// 图书摘要
export interface BookSummary {
  title: string;
  genre: string;
  themes: string[];
  tone: string;
  keyElements: string[];
  triggerWarnings: string[];
}

// 图书分析结果
export interface BookAnalysis {
  bookId: number;
  summary: BookSummary;
  posterUrl: string;
  recommendation: string;
  showPoster: boolean;
}

// 反馈历史
export interface FeedbackHistory {
  bookId: number;
  title: string;
  interested: boolean;
  reason?: string;
  timestamp: string;
}

// 分析历史（包含完整分析数据）
export interface AnalysisHistory {
  id: string;
  bookId: number;
  title: string;
  interested: boolean;
  analysisData: BookAnalysis;
  createdAt: string;
}

// 分页响应
export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// 用户偏好分析
export interface UserPreference {
  summary: string; // 总结文字
  readingReport: {
    totalBooks: number;
    interestedBooks: number;
    favoriteGenres: string[];
    favoriteThemes: string[];
    readingTrend: string;
  };
  annualReport?: AnnualReport;
}

// 年度报告
export interface AnnualReport {
  year: number;
  totalBooks: number;
  interestedCount: number;
  topGenres: { genre: string; count: number }[];
  topThemes: { theme: string; count: number }[];
  monthlyTrend: { month: number; count: number }[];
  highlights: string[];
}

// 图书API
export const bookApi = {
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

// Mock数据
export const mockBookApi = {
  analyzeBook: async (title: string) => {
    await new Promise(resolve => setTimeout(resolve, 2000));

    const feedbackCount = parseInt(localStorage.getItem('feedbackCount') || '0');

    const mockBooks: Record<string, BookAnalysis> = {
      '三体': {
        bookId: 1,
        summary: {
          title: '三体',
          genre: '科幻',
          themes: ['宇宙文明', '科技哲学', '人性探索', '生存困境'],
          tone: '深邃宏大',
          keyElements: ['黑暗森林法则', '三体文明', '降维打击', '宇宙社会学'],
          triggerWarnings: [],
        },
        posterUrl: 'https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800',
        recommendation:
          feedbackCount >= 3
            ? '基于你的阅读偏好,这本硬核科幻可能会让你着迷!'
            : '让我们一起探索这本书的魅力',
        showPoster: feedbackCount < 3 ? true : Math.random() > 0.3,
      },
      '活着': {
        bookId: 2,
        summary: {
          title: '活着',
          genre: '现实主义',
          themes: ['生命意义', '苦难', '家庭', '时代变迁'],
          tone: '沉重压抑',
          keyElements: ['福贵的一生', '历史洪流', '生存韧性'],
          triggerWarnings: ['含有悲剧情节'],
        },
        posterUrl: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800',
        recommendation: '这类现实主义题材你上次说太压抑,这次跳过。',
        showPoster: feedbackCount >= 3 ? Math.random() > 0.6 : true,
      },
      '解忧杂货店': {
        bookId: 3,
        summary: {
          title: '解忧杂货店',
          genre: '治愈系',
          themes: ['时空穿越', '人生抉择', '温情', '救赎'],
          tone: '温暖治愈',
          keyElements: ['神奇信箱', '跨时空对话', '人生困惑'],
          triggerWarnings: [],
        },
        posterUrl: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800',
        recommendation:
          feedbackCount >= 3
            ? '温馨治愈的故事,符合你喜欢轻松阅读的偏好!'
            : '一个充满温情的奇幻故事',
        showPoster: feedbackCount < 3 ? true : Math.random() > 0.2,
      },
      '人类简史': {
        bookId: 4,
        summary: {
          title: '人类简史',
          genre: '历史/科普',
          themes: ['人类演化', '社会发展', '认知革命', '未来展望'],
          tone: '理性思辨',
          keyElements: ['智人崛起', '农业革命', '科学革命', '虚构故事'],
          triggerWarnings: [],
        },
        posterUrl: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800',
        recommendation: '知识密度高的历史读物,适合你的求知欲!',
        showPoster: true,
      },
    };

    const book = mockBooks[title] || mockBooks['人类简史'];

    return {
      code: 200,
      message: '分析完成',
      ts: Date.now(),
      data: book,
      success: true,
    };
  },

  submitFeedback: async (_bookId: number, _interested: boolean, _reason?: string) => {
    await new Promise(resolve => setTimeout(resolve, 500));

    const count = parseInt(localStorage.getItem('feedbackCount') || '0');
    localStorage.setItem('feedbackCount', (count + 1).toString());

    return {
      code: 200,
      message: '反馈提交成功',
      ts: Date.now(),
      data: null,
      success: true,
    };
  },

  // Mock: 获取分析历史
  getAnalysisHistory: async (page: number, pageSize: number) => {
    await new Promise(resolve => setTimeout(resolve, 500));

    const historyStr = localStorage.getItem('analysisHistory') || '[]';
    const allHistory: AnalysisHistory[] = JSON.parse(historyStr);

    const total = allHistory.length;
    const totalPages = Math.ceil(total / pageSize);
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const items = allHistory.slice(start, end);

    return {
      code: 200,
      message: '获取成功',
      ts: Date.now(),
      data: {
        items,
        total,
        page,
        pageSize,
        totalPages,
      },
      success: true,
    };
  },

  // Mock: 获取用户偏好
  getUserPreference: async () => {
    await new Promise(resolve => setTimeout(resolve, 800));

    const historyStr = localStorage.getItem('analysisHistory') || '[]';
    const allHistory: AnalysisHistory[] = JSON.parse(historyStr);

    const totalBooks = allHistory.length;
    const interestedBooks = allHistory.filter(h => h.interested).length;

    // 统计类型和主题
    const genreMap: Record<string, number> = {};
    const themeMap: Record<string, number> = {};

    allHistory.forEach(h => {
      const genre = h.analysisData.summary.genre;
      genreMap[genre] = (genreMap[genre] || 0) + 1;

      h.analysisData.summary.themes.forEach(theme => {
        themeMap[theme] = (themeMap[theme] || 0) + 1;
      });
    });

    const favoriteGenres = Object.entries(genreMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([genre]) => genre);

    const favoriteThemes = Object.entries(themeMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([theme]) => theme);

    let summary = '';
    if (totalBooks === 0) {
      summary = '你还没有分析过任何书籍。开始探索吧！';
    } else if (totalBooks < 5) {
      summary = `你已经分析了 ${totalBooks} 本书，其中 ${interestedBooks} 本引起了你的兴趣。继续探索更多好书吧！`;
    } else {
      const interestRate = ((interestedBooks / totalBooks) * 100).toFixed(0);
      summary = `你是一位${favoriteGenres[0] || '多元化'}阅读爱好者，已分析 ${totalBooks} 本书，兴趣率达 ${interestRate}%。你特别关注${favoriteThemes.slice(0, 3).join('、')}等主题。`;
    }

    // 年度报告
    const currentYear = new Date().getFullYear();
    const yearHistory = allHistory.filter(h =>
      new Date(h.createdAt).getFullYear() === currentYear
    );

    const monthlyTrend = Array.from({ length: 12 }, (_, i) => ({
      month: i + 1,
      count: yearHistory.filter(h => new Date(h.createdAt).getMonth() === i).length,
    }));

    const yearGenreMap: Record<string, number> = {};
    const yearThemeMap: Record<string, number> = {};

    yearHistory.forEach(h => {
      const genre = h.analysisData.summary.genre;
      yearGenreMap[genre] = (yearGenreMap[genre] || 0) + 1;

      h.analysisData.summary.themes.forEach(theme => {
        yearThemeMap[theme] = (yearThemeMap[theme] || 0) + 1;
      });
    });

    const topGenres = Object.entries(yearGenreMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([genre, count]) => ({ genre, count }));

    const topThemes = Object.entries(yearThemeMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([theme, count]) => ({ theme, count }));

    const annualReport: AnnualReport = {
      year: currentYear,
      totalBooks: yearHistory.length,
      interestedCount: yearHistory.filter(h => h.interested).length,
      topGenres,
      topThemes,
      monthlyTrend,
      highlights: [
        `${currentYear}年，你探索了 ${yearHistory.length} 本书`,
        `最喜欢的类型：${topGenres[0]?.genre || '暂无'}`,
        `最关注的主题：${topThemes.slice(0, 3).map(t => t.theme).join('、') || '暂无'}`,
      ],
    };

    return {
      code: 200,
      message: '获取成功',
      ts: Date.now(),
      data: {
        summary,
        readingReport: {
          totalBooks,
          interestedBooks,
          favoriteGenres,
          favoriteThemes,
          readingTrend: totalBooks > 10 ? '持续增长' : totalBooks > 5 ? '稳步探索' : '刚刚起步',
        },
        annualReport,
      },
      success: true,
    };
  },
};
