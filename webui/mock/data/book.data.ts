/**
 * Mock数据 - 图书数据
 */
import { BookAnalysis, BookSummary } from '../src/models';

// Mock图书数据库
export const mockBooksData: Record<string, BookAnalysis> = {
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
    recommendation: '基于你的阅读偏好,这本硬核科幻可能会让你着迷!',
    showPoster: true,
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
    showPoster: true,
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
    recommendation: '温馨治愈的故事,符合你喜欢轻松阅读的偏好!',
    showPoster: true,
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

// 获取图书数据（支持根据反馈历史动态调整推荐）
export function getMockBook(title: string, feedbackCount: number = 0): BookAnalysis {
  const book = mockBooksData[title] || mockBooksData['人类简史'];

  // 根据反馈数量动态调整推荐
  if (feedbackCount >= 3) {
    if (title === '三体') {
      return { ...book, showPoster: Math.random() > 0.3 };
    } else if (title === '解忧杂货店') {
      return { ...book, showPoster: Math.random() > 0.2 };
    } else if (title === '活着') {
      return { ...book, showPoster: Math.random() > 0.6 };
    }
  }

  return book;
}
