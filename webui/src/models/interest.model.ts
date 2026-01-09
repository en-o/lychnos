/**
 * 分析相关的
 */

// 提交用户分析
export interface InterestFeedback {
  //书籍分析ID
  bookAnalyseId: string,
  // 书名
  bookTitle: string,
  // 是否感兴趣
  interested: boolean,
  // 理由
  reason?: string
}
