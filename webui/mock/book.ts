/**
 * Mock接口 - 图书相关
 */
import { MockMethod } from 'vite-plugin-mock';
import { getMockBook } from './data/book.data';
import type { Result, PageResult, AnalysisHistory, BookAnalysis, UserPreference, FeedbackHistory } from '../src/models';

// 使用内存存储模拟持久化（实际项目中可以用localStorage）
const analysisHistory: AnalysisHistory[] = [];
let feedbackCount = 0;

export default [
  // 获取快速推荐书籍列表
  {
    url: '/book/quick',
    method: 'get',
    timeout: 300,
    response: (): Result<string[]> => {
      return {
        code: 200,
        message: '获取成功',
        ts: Date.now(),
        data: ['三体', '活着', '解忧杂货店', '人类简史'],
        success: true,
      };
    },
  },

  // 分析图书
  {
    url: '/book/analyze',
    method: 'post',
    timeout: 2000,
    response: ({ body }: any): Result<BookAnalysis> => {
      const { title } = body;
      const book = getMockBook(title, feedbackCount);

      return {
        code: 200,
        message: '分析完成',
        ts: Date.now(),
        data: book,
        success: true,
      };
    },
  },

  // 提交反馈
  {
    url: '/book/feedback',
    method: 'post',
    timeout: 500,
    response: ({ body }: any): Result<null> => {
      const { bookId, interested } = body;
      feedbackCount++;

      // 记录到历史（简化版，实际应该从analyze结果中获取完整数据）
      const historyItem: any = {
        id: Date.now().toString(),
        bookId,
        title: '书籍标题',
        interested,
        analysisData: getMockBook('人类简史', feedbackCount),
        createdAt: new Date().toISOString(),
      };
      analysisHistory.unshift(historyItem);

      return {
        code: 200,
        message: '反馈提交成功',
        ts: Date.now(),
        data: null,
        success: true,
      };
    },
  },

  // 获取反馈历史
  {
    url: '/book/feedback/history',
    method: 'get',
    timeout: 500,
    response: (): Result<FeedbackHistory[]> => {
      const feedbackHistory: FeedbackHistory[] = analysisHistory.map(item => ({
        bookId: item.bookId,
        title: item.title,
        interested: item.interested,
        timestamp: item.createdAt,
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
    url: '/book/history',
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

  // 获取用户偏好
  {
    url: '/user/preference',
    method: 'get',
    timeout: 800,
    response: (): Result<UserPreference> => {
      const totalBooks = analysisHistory.length;
      const interestedBooks = analysisHistory.filter(h => h.interested).length;

      // 统计类型和主题
      const genreMap: Record<string, number> = {};
      const themeMap: Record<string, number> = {};

      analysisHistory.forEach(h => {
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
      const yearHistory = analysisHistory.filter(h =>
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
          interestedCount: yearHistory.filter(h => h.interested).length,
          topGenres,
          topThemes,
          monthlyTrend,
          highlights: [
            `${currentYear}年，你探索了 ${yearHistory.length} 本书`,
            `最喜欢的类型：${topGenres[0]?.genre || '暂无'}`,
            `最关注的主题：${topThemes.slice(0, 3).map(t => t.theme).join('、') || '暂无'}`,
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
    url: '/user/report/:year/download',
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
