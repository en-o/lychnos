/**
 * 公共实体类 - 对照Java实体
 * 参考路径:
 * - cn.tannn.jdevelops.result.response.ResultVO
 * - cn.tannn.jdevelops.result.response.PageResult
 * - cn.tannn.jdevelops.result.request.Paging
 */

/**
 * 通用返回结果包裹类 (对应 ResultVO<T>)
 */
export class Result<T = never> {
  /** 状态码 */
  code: number;
  /** 接口消息 */
  message: string;
  /** 时间戳 */
  ts: number;
  /** 数据 */
  data: T;
  /** 链路追踪ID */
  traceId?: string;

  constructor(data: Partial<Result<T>> = {}) {
    this.code = data.code || 0;
    this.message = data.message || '';
    this.ts = data.ts || Date.now();
    this.data = data.data as T;
    this.traceId = data.traceId;
  }

  /** 判断是否成功 (code === 200) */
  get success(): boolean {
    return this.code === 200;
  }
}

/**
 * 分页结果类 (对应 PageResult<T>)
 */
export class PageResult<T> {
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

  constructor(data: Partial<PageResult<T>> = {}) {
    this.currentPage = data.currentPage || 1;
    this.pageSize = data.pageSize || 20;
    this.totalPages = data.totalPages || 0;
    this.total = data.total || 0;
    this.rows = data.rows || [];
  }

  /** 创建分页结果 */
  static of<T>(
    currentPage: number,
    pageSize: number,
    total: number,
    rows: T[]
  ): PageResult<T> {
    const totalPages = Math.ceil(total / pageSize);
    return new PageResult<T>({
      currentPage,
      pageSize,
      totalPages,
      total,
      rows,
    });
  }

  /** 从Paging和数据创建分页结果 */
  static fromPaging<T>(paging: Paging, total: number, rows: T[]): PageResult<T> {
    return PageResult.of(paging.pageIndex, paging.pageSize, total, rows);
  }
}

/**
 * 分页请求参数类 (对应 Paging)
 */
export class Paging {
  /** 页码 [1-10000]，默认1 */
  pageIndex: number;
  /** 每页数量 [1-100]，默认20 */
  pageSize: number;

  constructor(data: Partial<Paging> = {}) {
    this.pageIndex = data.pageIndex && data.pageIndex >= 1 ? data.pageIndex : 1;
    this.pageSize = data.pageSize && data.pageSize >= 1 ? data.pageSize : 20;
  }

  /** 默认分页参数 */
  static default(): Paging {
    return new Paging({ pageIndex: 1, pageSize: 20 });
  }

  /** 创建分页参数 */
  static of(pageIndex: number, pageSize: number = 20): Paging {
    return new Paging({ pageIndex, pageSize });
  }
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
 */
export class PagingSorted extends Paging {
  /** 排序参数列表 */
  sorted?: Sorted[];

  constructor(data: Partial<PagingSorted> = {}) {
    super(data);
    this.sorted = data.sorted;
  }

  /** 创建分页排序参数 */
  static of(
    pageIndex: number,
    pageSize: number = 20,
    sorted?: Sorted[]
  ): PagingSorted {
    return new PagingSorted({ pageIndex, pageSize, sorted });
  }
}
