/**
 * 业务错误码常量
 * 与后端 BusinessErrorCode 枚举保持一致
 */

/**
 * 1001: 书籍已分析过
 */
export const BOOK_ALREADY_ANALYZED = 1001;

/**
 * 1002: 用户未配置可用的 AI 模型
 */
export const MODEL_NOT_CONFIGURED = 1002;

/**
 * 1003: 书籍不在推荐列表中（未登录用户限制）
 */
export const BOOK_NOT_IN_RECOMMENDATION = 1003;

/**
 * 1004: 书籍未找到分析数据
 */
export const BOOK_ANALYSIS_NOT_FOUND = 1004;

/**
 * 错误码类型定义
 */
export type BusinessErrorCode =
  | typeof BOOK_ALREADY_ANALYZED
  | typeof MODEL_NOT_CONFIGURED
  | typeof BOOK_NOT_IN_RECOMMENDATION
  | typeof BOOK_ANALYSIS_NOT_FOUND;
