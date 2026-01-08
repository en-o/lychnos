import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, ChevronLeft, ChevronRight, Eye, ThumbsDown, ThumbsUp} from 'lucide-react';
import {bookApi} from '../api/book';
import type {AnalysisHistory, PageResult} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';

const HistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [historyData, setHistoryData] = useState<PageResult<AnalysisHistory> | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedItem, setSelectedItem] = useState<AnalysisHistory | null>(null);
  const pageSize = 10;

  useEffect(() => {
    loadHistory(currentPage);
  }, [currentPage]);

  const loadHistory = async (page: number) => {
    setLoading(true);
    try {
      const response = await bookApi.getAnalysisHistory(page, pageSize);
      if (response.success) {
        setHistoryData(response.data);
      } else {
        toast.error('加载失败');
      }
    } catch (error) {
      console.error('加载历史失败:', error);
      toast.error('加载失败,请重试');
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (page: number) => {
    if (page < 1 || (historyData && page > historyData.totalPages)) return;
    setCurrentPage(page);
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
          {loading && currentPage === 1 ? (
            <div className="text-center py-12">
              <div className="inline-block w-8 h-8 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
              <p className="text-gray-600 mt-4">加载中...</p>
            </div>
          ) : historyData && historyData.rows.length > 0 ? (
            <>
              {/* 统计信息 */}
              <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
                <div className="flex items-center justify-between text-sm text-gray-600">
                  <span>共 {historyData.total} 条分析记录</span>
                  <span>
                    感兴趣: {historyData.rows.filter(h => h.interested).length} / 不感兴趣:{' '}
                    {historyData.rows.filter(h => !h.interested).length}
                  </span>
                </div>
              </div>

              {/* 历史列表 */}
              <div className="space-y-3">
                {historyData.rows.map((item) => (
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
                            {item.analysisData.summary.genre}
                          </span>
                          <span className="px-2 py-1 bg-purple-50 text-purple-700 rounded text-xs">
                            {item.analysisData.summary.tone}
                          </span>
                        </div>

                        <p className="text-sm text-gray-600 mb-2">
                          {item.analysisData.summary.themes.slice(0, 3).join('、')}
                        </p>

                        <p className="text-xs text-gray-500">
                          分析时间: {formatDate(item.createdAt)}
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

              {/* 分页 */}
              {historyData.totalPages > 1 && (
                <div className="mt-6 flex items-center justify-center gap-2">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                  >
                    <ChevronLeft className="w-5 h-5" />
                  </button>

                  <div className="flex gap-1">
                    {Array.from({ length: historyData.totalPages }, (_, i) => i + 1).map(
                      (page) => (
                        <button
                          key={page}
                          onClick={() => handlePageChange(page)}
                          className={`px-4 py-2 rounded-lg transition ${
                            page === currentPage
                              ? 'bg-blue-600 text-white'
                              : 'border border-gray-300 hover:bg-gray-50'
                          }`}
                        >
                          {page}
                        </button>
                      )
                    )}
                  </div>

                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === historyData.totalPages}
                    className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                  >
                    <ChevronRight className="w-5 h-5" />
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-600 mb-4">暂无分析历史</p>
              <button
                onClick={() => navigate('/')}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
              >
                开始分析
              </button>
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
                  {selectedItem.analysisData.summary.genre}
                </span>
                <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                  {selectedItem.analysisData.summary.tone}
                </span>
              </div>

              <div className="p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500">
                <p className="text-gray-800">{selectedItem.analysisData.recommendation}</p>
              </div>

              <div>
                <h3 className="text-sm font-medium text-gray-900 mb-2">核心主题</h3>
                <div className="flex flex-wrap gap-2">
                  {selectedItem.analysisData.summary.themes.map((theme, i) => (
                    <span key={i} className="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-sm">
                      {theme}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="text-sm font-medium text-gray-900 mb-2">关键元素</h3>
                <ul className="space-y-1">
                  {selectedItem.analysisData.summary.keyElements.map((element, i) => (
                    <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                      <span className="w-1 h-1 bg-gray-400 rounded-full" />
                      {element}
                    </li>
                  ))}
                </ul>
              </div>

              {selectedItem.analysisData.summary.triggerWarnings.length > 0 && (
                <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg">
                  <p className="text-sm text-amber-800">
                    ⚠️ {selectedItem.analysisData.summary.triggerWarnings.join('、')}
                  </p>
                </div>
              )}

              <div className="pt-4 border-t border-gray-200 text-sm text-gray-500">
                分析时间: {formatDate(selectedItem.createdAt)}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default HistoryPage;
