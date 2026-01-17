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
 * 错误码类型定义
 */
export type BusinessErrorCode =
  | typeof BOOK_ALREADY_ANALYZED
  | typeof MODEL_NOT_CONFIGURED;
