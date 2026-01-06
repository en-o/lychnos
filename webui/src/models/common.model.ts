/**
 * 公共实体类 - 对照Java实体
 * 参考路径:
 * - cn.tannn.jdevelops.result.response.ResultVO
 * - cn.tannn.jdevelops.result.response.PageResult
 * - cn.tannn.jdevelops.result.request.Paging
 */

/**
 * 通用返回结果包裹类 (对应 ResultVO<T>)
 * 所有接口返回都需要用Result包裹
 */
export interface Result<T = any> {
  /** 状态码 */
  code: number;
  /** 接口消息 */
  message: string;
  /** 时间戳 */
  ts: number;
  /** 数据 */
  data: T;
  /** 是否成功 */
  success: boolean;
  /** 链路追踪ID */
  traceId?: string;
}

/**
 * 分页结果类 (对应 PageResult<T>)
 * 用于接口返回分页数据
 */
export interface PageResult<T> {
  /** 当前页码 */
  currentPage: number;
  /** 每页显示条数 */
  pageSize: number;
  /** 总页数 */
  totalPages: number;
  /** 总记录数 */
  total: number;
  /** 数据列表 */
  rows: T[];
}

/**
 * 分页请求参数类 (对应 Paging)
 * 用于接口传参
 */
export interface Paging {
  /** 页码 [1-10000]，默认1 */
  pageIndex: number;
  /** 每页数量 [1-100]，默认20 */
  pageSize: number;
}

/**
 * 排序参数 (对应 Sorted)
 */
export interface Sorted {
  /** 排序字段 */
  orderBy: string[];
  /** 排序方向: 1ASC | 2DESC */
  orderDesc: 1 | 2;
}

/**
 * 分页+排序参数 (对应 PagingSorted)
 * 用于接口传参
 */
export interface PagingSorted extends Paging {
  /** 排序参数列表 */
  sorted?: Sorted[];
}
