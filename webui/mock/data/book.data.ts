/**
 * Mock数据 - 图书数据
 */
import { BookAnalysis, UserInterest, UserInterestDetail, BookRecommendItem } from '../../src/models';

// Mock书籍分析数据库（平台共享数据）
export const mockBookAnalysisData: BookAnalysis[] = [
  {
    id: '1001',
    title: '三体',
    genre: '科幻',
    themes: ['宇宙文明', '科技哲学', '人性探索', '生存困境'],
    tone: '深邃宏大',
    keyElements: ['黑暗森林法则', '三体文明', '降维打击', '宇宙社会学'],
    posterUrl: 'https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800',
    recommendation: '这是一部硬核科幻巨作，探讨了宇宙文明的终极法则和人类命运。刘慈欣以宏大的视角和深邃的思考，构建了一个震撼人心的宇宙图景。',
    createTime: '2024-01-15T10:00:00Z',
    createUser: 'system',
  },
  {
    id: '1002',
    title: '活着',
    genre: '现实主义',
    themes: ['生命意义', '苦难', '家庭', '时代变迁'],
    tone: '沉重压抑',
    keyElements: ['福贵的一生', '历史洪流', '生存韧性'],
    posterUrl: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800',
    recommendation: '余华用朴实的语言讲述了一个人一生的苦难与坚韧。这是一部关于生存、关于活着本身意义的深刻作品。',
    createTime: '2024-01-16T10:00:00Z',
    createUser: 'system',
  },
  {
    id: '1003',
    title: '解忧杂货店',
    genre: '治愈系',
    themes: ['时空穿越', '人生抉择', '温情', '救赎'],
    tone: '温暖治愈',
    keyElements: ['神奇信箱', '跨时空对话', '人生困惑'],
    posterUrl: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800',
    recommendation: '东野圭吾的温情之作，通过一个神奇的杂货店，连接起不同时空中迷茫的心灵。每个故事都传递着温暖与希望。',
    createTime: '2024-01-17T10:00:00Z',
    createUser: 'system',
  },
  {
    id: '1004',
    title: '人类简史',
    genre: '历史/科普',
    themes: ['人类演化', '社会发展', '认知革命', '未来展望'],
    tone: '理性思辨',
    keyElements: ['智人崛起', '农业革命', '科学革命', '虚构故事'],
    posterUrl: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800',
    recommendation: '尤瓦尔·赫拉利从宏观视角审视人类历史，用通俗的语言解读复杂的演化过程，引发对未来的深刻思考。',
    createTime: '2024-01-18T10:00:00Z',
    createUser: 'system',
  },
];

// Mock用户兴趣数据（用户与书籍分析的关联）
export const mockUserInterestData: UserInterest[] = [
  {
    id: '2001',
    userId: 'user_001',
    bookAnalyseId: '1001',
    interested: true,
    reason: '非常喜欢硬核科幻，三体系列一直是我的最爱',
    interestSummary: '用户对硬核科幻题材表现出浓厚兴趣，偏好宏大叙事和深度思考',
    createTime: '2024-02-01T15:30:00Z',
  },
  {
    id: '2002',
    userId: 'user_001',
    bookAnalyseId: '1002',
    interested: false,
    reason: '太沉重了，情感上难以承受',
    createTime: '2024-02-02T16:00:00Z',
  },
  {
    id: '2003',
    userId: 'user_001',
    bookAnalyseId: '1003',
    interested: true,
    reason: '很温暖的故事，适合放松时阅读',
    interestSummary: '用户喜欢治愈系作品，偏好温暖轻松的阅读体验',
    createTime: '2024-02-03T14:20:00Z',
  },
];

// 书籍推荐列表
export const mockBookRecommendList: BookRecommendItem[] = mockBookAnalysisData.map(book => ({
  id: book.id,
  title: book.title,
}));

// 获取书籍分析数据
export function getMockBookAnalysis(id: string): BookAnalysis | null {
  return mockBookAnalysisData.find(book => book.id === id) || null;
}

// 获取用户兴趣详情（包含书籍分析）
export function getMockUserInterestDetail(
  userId: string,
  bookAnalyseId: string
): UserInterestDetail | null {
  const bookAnalysis = getMockBookAnalysis(bookAnalyseId);
  if (!bookAnalysis) return null;

  const userInterest = mockUserInterestData.find(
    (item) => item.userId === userId && item.bookAnalyseId === bookAnalyseId
  );

  return {
    userInterest: userInterest || {
      id: '',
      userId,
      bookAnalyseId,
    },
    bookAnalysis,
    showPoster: true,
  };
}

// 获取用户所有兴趣列表
export function getMockUserInterests(userId: string): UserInterestDetail[] {
  return mockUserInterestData
    .filter((item) => item.userId === userId)
    .map((userInterest) => {
      const bookAnalysis = mockBookAnalysisData.find(book => book.id === userInterest.bookAnalyseId);
      return {
        userInterest,
        bookAnalysis: bookAnalysis!,
        showPoster: true,
      };
    })
    .filter((item) => item.bookAnalysis !== undefined);
}

// 添加或更新用户兴趣
export function addOrUpdateUserInterest(
  userId: string,
  bookAnalyseId: string,
  interested: boolean,
  reason?: string
): UserInterest {
  const existingIndex = mockUserInterestData.findIndex(
    (item) => item.userId === userId && item.bookAnalyseId === bookAnalyseId
  );

  const newInterest: UserInterest = {
    id: existingIndex >= 0 ? mockUserInterestData[existingIndex].id : `${Date.now()}`,
    userId,
    bookAnalyseId,
    interested,
    reason,
    createTime: existingIndex >= 0
      ? mockUserInterestData[existingIndex].createTime
      : new Date().toISOString(),
  };

  if (existingIndex >= 0) {
    mockUserInterestData[existingIndex] = newInterest;
  } else {
    mockUserInterestData.push(newInterest);
  }

  return newInterest;
}

// 获取随机书籍进行分析（模拟推荐场景）
export function getRandomBookForAnalysis(): BookAnalysis {
  return mockBookAnalysisData[Math.floor(Math.random() * mockBookAnalysisData.length)];
}
