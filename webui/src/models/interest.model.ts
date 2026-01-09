/**
 * 分析相关的
 */

// Token信息
export interface InterestFeedback {
  //书籍分析ID
  bookAnalyseId: string,
  // 是否感兴趣
  interested: boolean,
  // 理由
  reason?: string
}
