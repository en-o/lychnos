import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, Heart, XCircle, Search, TrendingUp, Clock, Sparkles, LogOut } from 'lucide-react';
import { mockBookApi, type BookAnalysis, type FeedbackHistory } from '../api/book';

const BookReadingAssistant: React.FC = () => {
  const navigate = useNavigate();
  const [bookTitle, setBookTitle] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<BookAnalysis | null>(null);
  const [feedbackHistory, setFeedbackHistory] = useState<FeedbackHistory[]>([]);
  const [showHistory, setShowHistory] = useState(false);

  const quickBooks = ['三体', '活着', '解忧杂货店', '人类简史'];

  const handleSearch = async (title = bookTitle) => {
    if (!title.trim()) {
      alert('请输入书名');
      return;
    }

    setLoading(true);
    setResult(null);

    try {
      const response = await mockBookApi.analyzeBook(title);

      if (response.success) {
        setResult(response.data);
      } else {
        alert(response.message || '分析失败');
      }
    } catch (error) {
      console.error('分析失败:', error);
      alert('分析失败,请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleFeedback = async (interested: boolean, reason = '') => {
    if (!result) return;

    try {
      const response = await mockBookApi.submitFeedback(
        result.bookId,
        interested,
        reason
      );

      if (response.success) {
        // 更新本地反馈历史
        const newFeedback: FeedbackHistory = {
          bookId: result.bookId,
          title: result.summary.title,
          interested,
          reason,
          timestamp: new Date().toISOString(),
        };

        setFeedbackHistory((prev) => [newFeedback, ...prev]);

        alert('✅ 反馈已提交!系统会根据你的偏好优化推荐');
        setResult(null);
        setBookTitle('');
      }
    } catch (error) {
      console.error('提交失败:', error);
      alert('提交失败,请重试');
    }
  };

  const handleQuickSearch = (title: string) => {
    setBookTitle(title);
    handleSearch(title);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 p-4 md:p-8">
      <div className="max-w-5xl mx-auto">
        {/* 头部 */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-3">
            <BookOpen className="w-10 h-10 text-indigo-600" />
            <h1 className="text-4xl md:text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              图书阅读助理
            </h1>
            <button
              onClick={handleLogout}
              className="ml-auto px-4 py-2 text-gray-600 hover:text-gray-800 hover:bg-white/50 rounded-lg transition flex items-center gap-2"
              title="退出登录"
            >
              <LogOut className="w-5 h-5" />
              退出
            </button>
          </div>
          <p className="text-gray-600">AI 驱动的个性化阅读推荐系统</p>
          <div className="mt-4 flex items-center justify-center gap-4 text-sm text-gray-500">
            <div className="flex items-center gap-1">
              <Sparkles className="w-4 h-4" />
              <span>智能分析</span>
            </div>
            <div className="flex items-center gap-1">
              <TrendingUp className="w-4 h-4" />
              <span>偏好学习</span>
            </div>
            <div className="flex items-center gap-1">
              <Clock className="w-4 h-4" />
              <span>快速决策</span>
            </div>
          </div>
        </div>

        {/* 搜索区域 */}
        <div className="bg-white rounded-2xl shadow-xl p-6 mb-6">
          <div className="flex gap-3 mb-4">
            <input
              type="text"
              value={bookTitle}
              onChange={(e) => setBookTitle(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="输入书名,探索是否值得阅读..."
              className="flex-1 px-5 py-4 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-indigo-500 transition text-lg"
              disabled={loading}
            />
            <button
              onClick={() => handleSearch()}
              disabled={loading || !bookTitle.trim()}
              className="px-8 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition font-medium flex items-center gap-2 shadow-lg"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  分析中
                </>
              ) : (
                <>
                  <Search className="w-5 h-5" />
                  分析
                </>
              )}
            </button>
          </div>

          {/* 快速选择 */}
          <div>
            <p className="text-sm text-gray-600 mb-2">快速体验:</p>
            <div className="flex flex-wrap gap-2">
              {quickBooks.map((book) => (
                <button
                  key={book}
                  onClick={() => handleQuickSearch(book)}
                  disabled={loading}
                  className="px-4 py-2 bg-indigo-50 text-indigo-700 rounded-lg hover:bg-indigo-100 transition text-sm font-medium disabled:opacity-50"
                >
                  {book}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* 结果展示 */}
        {result && (
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden animate-fadeIn">
            {result.showPoster ? (
              <div>
                {/* 画报模式 */}
                <div className="relative h-80 overflow-hidden">
                  <img
                    src={result.posterUrl}
                    alt={result.summary.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
                    <h2 className="text-3xl font-bold mb-2">
                      {result.summary.title}
                    </h2>
                    <div className="flex gap-2">
                      <span className="px-3 py-1 bg-white/20 backdrop-blur rounded-full text-sm">
                        {result.summary.genre}
                      </span>
                      <span className="px-3 py-1 bg-white/20 backdrop-blur rounded-full text-sm">
                        {result.summary.tone}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="p-6">
                  {/* 推荐语 */}
                  <div className="mb-6 p-4 bg-indigo-50 rounded-xl border-l-4 border-indigo-500">
                    <p className="text-indigo-900 font-medium">
                      💡 {result.recommendation}
                    </p>
                  </div>

                  {/* 主题标签 */}
                  <div className="mb-4">
                    <h3 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                      <span className="w-1 h-5 bg-indigo-600 rounded" />
                      核心主题
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {result.summary.themes.map((theme, i) => (
                        <span
                          key={i}
                          className="px-3 py-1.5 bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-800 rounded-lg text-sm font-medium"
                        >
                          {theme}
                        </span>
                      ))}
                    </div>
                  </div>

                  {/* 关键元素 */}
                  <div className="mb-6">
                    <h3 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                      <span className="w-1 h-5 bg-purple-600 rounded" />
                      关键元素
                    </h3>
                    <div className="grid grid-cols-2 gap-2">
                      {result.summary.keyElements.map((element, i) => (
                        <div
                          key={i}
                          className="flex items-center gap-2 text-sm text-gray-700"
                        >
                          <div className="w-1.5 h-1.5 bg-purple-500 rounded-full" />
                          {element}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* 警告信息 */}
                  {result.summary.triggerWarnings.length > 0 && (
                    <div className="mb-6 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                      <p className="text-sm text-amber-800">
                        ⚠️ {result.summary.triggerWarnings.join('、')}
                      </p>
                    </div>
                  )}

                  {/* 反馈按钮 */}
                  <div className="flex gap-3">
                    <button
                      onClick={() => handleFeedback(true)}
                      className="flex-1 px-6 py-4 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-xl hover:from-green-600 hover:to-emerald-600 transition font-medium flex items-center justify-center gap-2 shadow-lg"
                    >
                      <Heart className="w-5 h-5" />
                      感兴趣,想读
                    </button>
                    <button
                      onClick={() => {
                        const reason = prompt('请简单说明不感兴趣的原因(可选):');
                        handleFeedback(false, reason || '');
                      }}
                      className="flex-1 px-6 py-4 bg-gradient-to-r from-red-500 to-pink-500 text-white rounded-xl hover:from-red-600 hover:to-pink-600 transition font-medium flex items-center justify-center gap-2 shadow-lg"
                    >
                      <XCircle className="w-5 h-5" />
                      不感兴趣
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-12 text-center">
                {/* 语录模式 */}
                <div className="inline-block p-4 bg-gray-100 rounded-full mb-6">
                  <XCircle className="w-16 h-16 text-gray-400" />
                </div>
                <h2 className="text-2xl font-bold mb-3 text-gray-800">
                  《{result.summary.title}》
                </h2>
                <p className="text-lg text-gray-600 mb-8 max-w-md mx-auto">
                  {result.recommendation}
                </p>
                <button
                  onClick={() => {
                    setResult(null);
                    setBookTitle('');
                  }}
                  className="px-6 py-3 bg-gray-600 text-white rounded-xl hover:bg-gray-700 transition font-medium"
                >
                  继续探索其他书籍
                </button>
              </div>
            )}
          </div>
        )}

        {/* 反馈历史 */}
        {feedbackHistory.length > 0 && (
          <div className="mt-6 bg-white rounded-2xl shadow-xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                <Clock className="w-5 h-5 text-indigo-600" />
                反馈历史
                <span className="px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded-full text-sm">
                  {feedbackHistory.length}
                </span>
              </h3>
              <button
                onClick={() => setShowHistory(!showHistory)}
                className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
              >
                {showHistory ? '收起' : '展开'}
              </button>
            </div>

            {showHistory && (
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {feedbackHistory.map((item, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
                  >
                    <div className="text-2xl">
                      {item.interested ? '❤️' : '❌'}
                    </div>
                    <div className="flex-1">
                      <div className="font-medium text-gray-800">
                        {item.title}
                      </div>
                      {item.reason && (
                        <div className="text-sm text-gray-600 mt-1">
                          理由:{item.reason}
                        </div>
                      )}
                    </div>
                    <div className="text-xs text-gray-400">
                      {new Date(item.timestamp).toLocaleString('zh-CN', {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 说明卡片 */}
        <div className="mt-6 bg-white/50 backdrop-blur rounded-xl p-6 text-sm text-gray-600">
          <h4 className="font-semibold text-gray-800 mb-2">💡 使用说明</h4>
          <ul className="space-y-1">
            <li>• 前 10 本书会为你生成完整画报,帮助系统学习你的偏好</li>
            <li>• 数据足够后,系统会智能过滤不符合口味的书籍,节省你的时间</li>
            <li>• 每次反馈都会让推荐更精准,建议如实填写不感兴趣的原因</li>
          </ul>
        </div>
      </div>

      <style>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fadeIn {
          animation: fadeIn 0.5s ease-out;
        }
      `}</style>
    </div>
  );
};

export default BookReadingAssistant;
