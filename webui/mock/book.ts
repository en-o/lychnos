/**
 * Mock接口 - 图书相关
 */
import { MockMethod } from 'vite-plugin-mock';
import {
  getMockBookAnalysis,
  getMockUserInterestDetail,
  getMockUserInterests,
  addOrUpdateUserInterest,
  getRandomBookForAnalysis,
  mockBookAnalysisData,
  mockBookRecommendList,
} from './data/book.data';
import type {
  AnalysisHistory,
  BookAnalysis,
  BookRecommendItem,
  UserInterest,
  UserInterestDetail,
  FeedbackHistory,
  PageResult,
  Result,
  UserPreference,
} from '../src/models';

// 使用内存存储模拟持久化
const analysisHistory: AnalysisHistory[] = [];

export default [
  // 获取快速推荐书籍列表（返回 id+title）
  {
    url: '/api/book/recommend',
    method: 'get',
    timeout: 300,
    response: (): Result<BookRecommendItem[]> => {
      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: mockBookRecommendList,
        success: true,
      };
    },
  },

  {
    // 分析图书（通过 title 查询或创建书籍分析）
    url: '/api/book/analyze/:title',   // ① 动态路由
    method: 'put',
    timeout: 2000,
    response: ({ params }: any): Result<BookAnalysis> => {
      const { title } = params;          // ② 从路径里拿 title

      // 这里只是演示：如果 title 是三体就返回三体，否则随机给一本
      let bookAnalysis = mockBookAnalysisData.find(b => b.title === title);
      if (!bookAnalysis) {
        bookAnalysis = getRandomBookForAnalysis(); // 你已有的随机函数
      }

      return {
        code: 200,
        message: '分析完成',
        ts: Date.now(),
        data: bookAnalysis,
        success: true,
      };
    },
  },

  // 获取书籍分析详情
  {
    url: '/api/book/analysis/:id',
    method: 'get',
    timeout: 300,
    response: ({ query }: any): Result<BookAnalysis | null> => {
      const id = query.id;
      const bookAnalysis = getMockBookAnalysis(id);

      if (!bookAnalysis) {
        return {
          code: 404,
          message: '书籍分析不存在',
          ts: Date.now(),
          data: null,
          success: false,
        };
      }

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: bookAnalysis,
        success: true,
      };
    },
  },

  // 提交用户反馈（创建或更新用户兴趣）
  {
    url: '/api/user/interest',
    method: 'post',
    timeout: 500,
    response: ({ body }: any): Result<UserInterest> => {
      const { bookAnalyseId, interested, reason } = body;
      const userId = 'user_001'; // 实际项目中从token获取用户ID

      const userInterest = addOrUpdateUserInterest(
        userId,
        bookAnalyseId,
        interested,
        reason
      );

      // 同时更新历史记录
      const bookAnalysis = mockBookAnalysisData.find(book => book.id === bookAnalyseId);

      if (bookAnalysis) {
        const existingHistoryIndex = analysisHistory.findIndex(
          (h) => h.id === bookAnalysis.id
        );

        const historyItem: AnalysisHistory = {
          id: userInterest.id,
          title: bookAnalysis.title,
          interested: interested ?? false,
          analysisData: bookAnalysis,
          createTime: userInterest.createTime || new Date().toISOString(),
        };

        if (existingHistoryIndex >= 0) {
          analysisHistory[existingHistoryIndex] = historyItem;
        } else {
          analysisHistory.unshift(historyItem);
        }
      }

      return {
        code: 200,
        message: '反馈提交成功',
        ts: Date.now(),
        data: userInterest,
        success: true,
      };
    },
  },

  // 获取用户兴趣详情（包含书籍分析）
  {
    url: '/api/user/interest/:bookAnalyseId',
    method: 'get',
    timeout: 300,
    response: ({ query }: any): Result<UserInterestDetail | null> => {
      const bookAnalyseId = query.bookAnalyseId;
      const userId = 'user_001';

      const detail = getMockUserInterestDetail(userId, bookAnalyseId);

      if (!detail) {
        return {
          code: 404,
          message: '数据不存在',
          ts: Date.now(),
          data: null,
          success: false,
        };
      }

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: detail,
        success: true,
      };
    },
  },

  // 获取用户所有兴趣列表
  {
    url: '/api/user/interests',
    method: 'get',
    timeout: 300,
    response: (): Result<UserInterestDetail[]> => {
      const userId = 'user_001';
      const interests = getMockUserInterests(userId);

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: interests,
        success: true,
      };
    },
  },

  // 获取反馈历史（简化版）
  {
    url: '/api/book/feedback/history',
    method: 'get',
    timeout: 500,
    response: (): Result<FeedbackHistory[]> => {
      const feedbackHistory: FeedbackHistory[] = analysisHistory.map((item) => ({
        id: item.id,
        title: item.title,
        interested: item.interested,
        timestamp: item.createTime,
      }));

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: feedbackHistory,
        success: true,
      };
    },
  },

  // 获取分析历史（分页）
  {
    url: '/api/book/history',
    method: 'get',
    timeout: 500,
    response: ({ query }: any): Result<PageResult<AnalysisHistory>> => {
      const page = parseInt(query.page) || 1;
      const pageSize = parseInt(query.pageSize) || 10;

      const total = analysisHistory.length;
      const totalPages = Math.ceil(total / pageSize);
      const start = (page - 1) * pageSize;
      const end = start + pageSize;
      const items = analysisHistory.slice(start, end);

      const pageResult: PageResult<AnalysisHistory> = {
        rows: items,
        total,
        currentPage: page,
        pageSize,
        totalPages,
      };

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: pageResult,
        success: true,
      };
    },
  },

  // 获取用户偏好分析
  {
    url: '/api/user/preference',
    method: 'get',
    timeout: 800,
    response: (): Result<UserPreference> => {
      const totalBooks = analysisHistory.length;
      const interestedBooks = analysisHistory.filter((h) => h.interested).length;

      // 统计类型和主题
      const genreMap: Record<string, number> = {};
      const themeMap: Record<string, number> = {};

      analysisHistory.forEach((h) => {
        const genre = h.analysisData.genre;
        genreMap[genre] = (genreMap[genre] || 0) + 1;

        h.analysisData.themes.forEach((theme) => {
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
      const yearHistory = analysisHistory.filter(
        (h) => new Date(h.createTime).getFullYear() === currentYear
      );

      const monthlyTrend = Array.from({ length: 12 }, (_, i) => ({
        month: i + 1,
        count: yearHistory.filter((h) => new Date(h.createTime).getMonth() === i).length,
      }));

      const yearGenreMap: Record<string, number> = {};
      const yearThemeMap: Record<string, number> = {};

      yearHistory.forEach((h) => {
        const genre = h.analysisData.genre;
        yearGenreMap[genre] = (yearGenreMap[genre] || 0) + 1;

        h.analysisData.themes.forEach((theme) => {
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

      const preference: UserPreference = {
        summary,
        readingReport: {
          totalBooks,
          interestedBooks,
          favoriteGenres,
          favoriteThemes,
          readingTrend: totalBooks > 10 ? '持续增长' : totalBooks > 5 ? '稳步探索' : '刚刚起步',
        },
        annualReport: {
          year: currentYear,
          totalBooks: yearHistory.length,
          interestedCount: yearHistory.filter((h) => h.interested).length,
          topGenres,
          topThemes,
          monthlyTrend,
          highlights: [
            `${currentYear}年，你探索了 ${yearHistory.length} 本书`,
            `最喜欢的类型：${topGenres[0]?.genre || '暂无'}`,
            `最关注的主题：${topThemes.slice(0, 3).map((t) => t.theme).join('、') || '暂无'}`,
          ],
        },
      };

      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: preference,
        success: true,
      };
    },
  },

  // 下载年度报告
  {
    url: '/api/user/report/:year/download',
    method: 'get',
    timeout: 1000,
    response: (): Result<null> => {
      // 返回模拟的文件下载响应
      return {
        code: 200,
        message: '下载成功',
        ts: Date.now(),
        data: null,
        success: true,
      };
    },
  },
] as MockMethod[];
