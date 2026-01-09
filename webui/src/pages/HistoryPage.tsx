import React, {useEffect, useState, useRef, useCallback} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {ArrowLeft, Eye, ThumbsDown, ThumbsUp, Search, X} from 'lucide-react';
import {bookApi} from '../api/book';
import type {AnalysisHistory} from '../models';
import Logo from '../components/Logo';
import ImagePreview from '../components/ImagePreview';

const HistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [historyList, setHistoryList] = useState<AnalysisHistory[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [total, setTotal] = useState(0);
  const [selectedItem, setSelectedItem] = useState<AnalysisHistory | null>(null);
  const [imageError, setImageError] = useState<Record<string, boolean>>({});
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [urlSearchProcessed, setUrlSearchProcessed] = useState(false);
  const pageSize = 10;
  const observerTarget = useRef<HTMLDivElement>(null);

  // 从 URL 参数获取搜索关键词（只处理一次）
  useEffect(() => {
    const searchFromUrl = searchParams.get('search');
    if (searchFromUrl && !urlSearchProcessed) {
      setSearchInput(searchFromUrl);
      setSearchQuery(searchFromUrl);
      setUrlSearchProcessed(true);
    } else if (!urlSearchProcessed) {
      setUrlSearchProcessed(true);
    }
  }, [searchParams, urlSearchProcessed]);

  // 加载历史数据
  const loadHistory = useCallback(async (page: number, query: string, append: boolean = false) => {
    setLoading(true);
    try {
      const response = await bookApi.getAnalysisHistory(page, pageSize, query);
      if (response.success) {
        const newItems = response.data.rows;
        if (append) {
          setHistoryList(prev => [...prev, ...newItems]);
        } else {
          setHistoryList(newItems);
        }
        setTotal(response.data.total);
        setHasMore(page < response.data.totalPages);
      }
    } catch (error) {
      console.error('加载历史失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // 初始加载（当 searchQuery 变化时）
  useEffect(() => {
    if (!urlSearchProcessed) return; // 等待 URL 参数处理完成

    setLoading(true);
    setCurrentPage(1);
    setHistoryList([]);
    setHasMore(true);

    bookApi.getAnalysisHistory(1, pageSize, searchQuery)
      .then(response => {
        if (response.success) {
          setHistoryList(response.data.rows);
          setTotal(response.data.total);
          setHasMore(1 < response.data.totalPages);
        }
      })
      .catch(error => {
        console.error('加载历史失败:', error);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [searchQuery, urlSearchProcessed]);

  // 滚动加载更多
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          const nextPage = currentPage + 1;
          setCurrentPage(nextPage);
          loadHistory(nextPage, searchQuery, true);
        }
      },
      { threshold: 0.1 }
    );

    const currentTarget = observerTarget.current;
    if (currentTarget) {
      observer.observe(currentTarget);
    }

    return () => {
      if (currentTarget) {
        observer.unobserve(currentTarget);
      }
    };
  }, [hasMore, loading, currentPage, searchQuery, loadHistory]);

  // 处理搜索
  const handleSearch = () => {
    // 如果搜索内容相同，手动触发重新加载
    if (searchInput === searchQuery) {
      setCurrentPage(1);
      setHistoryList([]);
      loadHistory(1, searchInput);
    } else {
      setSearchQuery(searchInput);
    }
  };

  // 清除搜索
  const handleClearSearch = () => {
    setSearchInput('');
    setSearchQuery('');
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/')}
              className="p-1.5 hover:bg-gray-100 rounded-lg transition"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
            <Logo className="w-5 h-5" />
            <span className="font-semibold text-gray-800">我的历史</span>
          </div>
        </div>
      </nav>

      {/* 主内容 */}
      <main className="pt-14">
        <div className="max-w-6xl mx-auto px-4 py-8">
          {/* 搜索框 */}
          <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  placeholder="搜索书名..."
                  className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                {searchInput && (
                  <button
                    onClick={handleClearSearch}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    <X className="w-5 h-5" />
                  </button>
                )}
              </div>
              <button
                onClick={handleSearch}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
              >
                搜索
              </button>
            </div>
          </div>

          {loading && historyList.length === 0 ? (
            <div className="text-center py-12">
              <div className="inline-block w-8 h-8 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
              <p className="text-gray-600 mt-4">加载中...</p>
            </div>
          ) : historyList.length > 0 ? (
            <>
              {/* 统计信息 */}
              <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
                <div className="flex items-center justify-between text-sm text-gray-600">
                  <span>共 {total} 条分析记录</span>
                  <span>
                    感兴趣: {historyList.filter(h => h.interested).length} / 不感兴趣:{' '}
                    {historyList.filter(h => !h.interested).length}
                  </span>
                </div>
              </div>

              {/* 历史列表 */}
              <div className="space-y-3">
                {historyList.map((item) => (
                  <div
                    key={item.id}
                    className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="text-lg font-medium text-gray-900">
                            {item.title}
                          </h3>
                          {item.interested ? (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-green-100 text-green-700 rounded text-xs">
                              <ThumbsUp className="w-3 h-3" />
                              感兴趣
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs">
                              <ThumbsDown className="w-3 h-3" />
                              不感兴趣
                            </span>
                          )}
                        </div>

                        <div className="flex flex-wrap gap-2 mb-2">
                          <span className="px-2 py-1 bg-blue-50 text-blue-700 rounded text-xs">
                            {item.analysisData.genre}
                          </span>
                          <span className="px-2 py-1 bg-purple-50 text-purple-700 rounded text-xs">
                            {item.analysisData.tone}
                          </span>
                        </div>

                        <p className="text-sm text-gray-600 mb-2">
                          {item.analysisData.themes.slice(0, 3).join('、')}
                        </p>

                        <p className="text-xs text-gray-500">
                          分析时间: {formatDate(item.createTime)}
                        </p>
                      </div>

                      <button
                        onClick={() => setSelectedItem(item)}
                        className="ml-4 px-3 py-2 text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition flex items-center gap-1"
                      >
                        <Eye className="w-4 h-4" />
                        查看详情
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* 加载更多指示器 */}
              <div ref={observerTarget} className="py-8 text-center">
                {loading && (
                  <div className="inline-block w-6 h-6 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
                )}
                {!hasMore && historyList.length > 0 && (
                  <p className="text-gray-400 text-sm">已加载全部数据</p>
                )}
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-600 mb-4">
                {searchQuery ? '未找到相关记录' : '暂无分析历史'}
              </p>
              {!searchQuery && (
                <button
                  onClick={() => navigate('/')}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  开始分析
                </button>
              )}
            </div>
          )}
        </div>
      </main>

      {/* 详情弹窗 */}
      {selectedItem && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[10000] p-4">
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-start justify-between mb-4">
              <h2 className="text-2xl font-semibold text-gray-900">
                {selectedItem.title}
              </h2>
              <button
                onClick={() => setSelectedItem(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex gap-2">
                <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                  {selectedItem.analysisData.genre}
                </span>
                <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                  {selectedItem.analysisData.tone}
                </span>
              </div>

              {selectedItem.analysisData.posterUrl && (
                <div>
                  {!imageError[selectedItem.id] ? (
                    <img
                      src={selectedItem.analysisData.posterUrl}
                      alt={selectedItem.title}
                      className="w-full h-48 object-fill rounded-lg cursor-pointer hover:opacity-90 transition"
                      onError={() => setImageError(prev => ({ ...prev, [selectedItem.id]: true }))}
                      onClick={() => setPreviewImage(selectedItem.analysisData.posterUrl || null)}
                    />
                  ) : (
                    <div className="w-full h-48 bg-gray-100 rounded-lg flex items-center justify-center">
                      <div className="text-center text-gray-400">
                        <svg className="w-12 h-12 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p className="text-sm">图片加载失败</p>
                      </div>
                    </div>
                  )}
                </div>
              )}

              <div className="p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500">
                <p className="text-gray-800">{selectedItem.analysisData.recommendation}</p>
              </div>

              <div>
                <h3 className="text-sm font-medium text-gray-900 mb-2">核心主题</h3>
                <div className="flex flex-wrap gap-2">
                  {selectedItem.analysisData.themes.map((theme, i) => (
                    <span key={i} className="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-sm">
                      {theme}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="text-sm font-medium text-gray-900 mb-2">关键元素</h3>
                <ul className="space-y-1">
                  {selectedItem.analysisData.keyElements.map((element, i) => (
                    <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                      <span className="w-1 h-1 bg-gray-400 rounded-full" />
                      {element}
                    </li>
                  ))}
                </ul>
              </div>

              <div className="pt-4 border-t border-gray-200 text-sm text-gray-500">
                分析时间: {formatDate(selectedItem.createTime)}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 图片预览 */}
      {previewImage && (
        <ImagePreview
          src={previewImage}
          alt="图片预览"
          onClose={() => setPreviewImage(null)}
        />
      )}
    </div>
  );
};

export default HistoryPage;
