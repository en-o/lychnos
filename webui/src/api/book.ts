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
};
