import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  LogOut,
  UserCircle,
  Key,
  Brain,
  ChevronDown
} from 'lucide-react';
import { mockBookApi, type BookAnalysis, type FeedbackHistory } from '../api/book';
import Logo from '../components/Logo';
import { toast } from '../components/ToastContainer';
import ConfirmDialog from '../components/ConfirmDialog';

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [bookTitle, setBookTitle] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<BookAnalysis | null>(null);
  const [feedbackHistory, setFeedbackHistory] = useState<FeedbackHistory[]>([]);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showLoginConfirm, setShowLoginConfirm] = useState(false);

  // 检查是否已登录
  const token = localStorage.getItem('token');
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');

  const quickBooks = ['三体', '活着', '解忧杂货店', '人类简史'];

  const handleSearch = async (title = bookTitle) => {
    if (!title.trim()) {
      toast.warning('请输入书名');
      return;
    }

    // 检查登录状态
    if (!token) {
      setShowLoginConfirm(true);
      return;
    }

    setLoading(true);
    setResult(null);

    try {
      const response = await mockBookApi.analyzeBook(title);

      if (response.success) {
        setResult(response.data);
      } else {
        toast.error(response.message || '分析失败');
      }
    } catch (error) {
      console.error('分析失败:', error);
      toast.error('分析失败,请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleFeedback = async (interested: boolean) => {
    if (!result) return;

    try {
      const response = await mockBookApi.submitFeedback(
        result.bookId,
        interested,
        ''
      );

      if (response.success) {
        const newFeedback: FeedbackHistory = {
          bookId: result.bookId,
          title: result.summary.title,
          interested,
          reason: '',
          timestamp: new Date().toISOString(),
        };

        setFeedbackHistory((prev) => [newFeedback, ...prev]);
        toast.success('反馈已提交!');
        setResult(null);
        setBookTitle('');
      }
    } catch (error) {
      console.error('提交失败:', error);
      toast.error('提交失败,请重试');
    }
  };

  const handleQuickSearch = (title: string) => {
    setBookTitle(title);
    handleSearch(title);
  };

  const handleLogin = () => {
    navigate('/login?redirect=/');
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    setShowUserMenu(false);
    window.location.reload();
  };

  const handleMenuClick = (path: string) => {
    setShowUserMenu(false);
    navigate(path);
  };

  return (
    <div className="min-h-screen bg-white">
      {/* 顶部导航栏 - ChatGPT 风格 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <Logo className="w-5 h-5" />
            <span className="font-semibold text-gray-800">书灯</span>
          </div>

          {/* 用户区域 */}
          <div className="flex items-center gap-3">
            {token ? (
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition"
                >
                 ·
                  <ChevronDown className="w-4 h-4 text-gray-600" />
                </button>

                {/* 下拉菜单 */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="font-medium text-gray-900">{userInfo.nickname || userInfo.username}</p>
                      <p className="text-sm text-gray-500">{userInfo.email || userInfo.username}</p>
                    </div>

                    <button
                      onClick={() => handleMenuClick('/profile')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <UserCircle className="w-4 h-4" />
                      个人资料
                    </button>

                    <button
                      onClick={() => handleMenuClick('/settings/models')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <Brain className="w-4 h-4" />
                      AI模型设置
                    </button>

                    <button
                      onClick={() => handleMenuClick('/settings/password')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <Key className="w-4 h-4" />
                      修改密码
                    </button>

                    <div className="border-t border-gray-100 mt-1 pt-1">
                      <button
                        onClick={handleLogout}
                        className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-gray-50 flex items-center gap-3"
                      >
                        <LogOut className="w-4 h-4" />
                        退出登录
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <button
                onClick={handleLogin}
                className="px-4 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition"
              >
                登录
              </button>
            )}
          </div>
        </div>
      </nav>

      {/* 主内容区域 */}
      <main className="pt-14">
        <div className="max-w-3xl mx-auto px-4 py-8">
          {/* 欢迎区域 */}
          {!result && (
            <div className="text-center py-12">
              <p className="text-gray-600 mb-8">
                在翻开书之前，先点一盏灯
              </p>

              {/* 搜索框 */}
              <div className="max-w-2xl mx-auto mb-6">
                <div className="relative">
                  <input
                    type="text"
                    value={bookTitle}
                    onChange={(e) => setBookTitle(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    placeholder="输入书名,开始分析..."
                    className="w-full px-5 py-3.5 pr-12 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-base"
                    disabled={loading}
                  />
                  <button
                    onClick={() => handleSearch()}
                    disabled={loading || !bookTitle.trim()}
                    className="absolute right-2 top-1/2 -translate-y-1/2 p-2 text-gray-400 hover:text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? (
                      <div className="w-5 h-5 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin" />
                    ) : (
                      <Search className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* 快速选择 */}
              <div className="flex items-center justify-center gap-2 flex-wrap">
                <span className="text-sm text-gray-500">试试:</span>
                {quickBooks.map((book) => (
                  <button
                    key={book}
                    onClick={() => handleQuickSearch(book)}
                    disabled={loading}
                    className="px-3 py-1.5 text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition disabled:opacity-50"
                  >
                    {book}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* 结果展示 */}
          {result && (
            <div className="animate-fadeIn">
              {/* 返回按钮 */}
              <button
                onClick={() => {
                  setResult(null);
                  setBookTitle('');
                }}
                className="mb-6 text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1"
              >
                ← 返回搜索
              </button>

              {/* 书籍信息卡片 */}
              <div className="bg-white border border-gray-200 rounded-xl p-6 mb-4">
                {result.showPoster && (
                  <div className="mb-6">
                    <img
                      src={result.posterUrl}
                      alt={result.summary.title}
                      className="w-full h-64 object-cover rounded-lg"
                    />
                  </div>
                )}

                <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                  {result.summary.title}
                </h2>

                <div className="flex gap-2 mb-4">
                  <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                    {result.summary.genre}
                  </span>
                  <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                    {result.summary.tone}
                  </span>
                </div>

                {/* 推荐语 */}
                <div className="mb-6 p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500">
                  <p className="text-gray-800">{result.recommendation}</p>
                </div>

                {/* 主题 */}
                <div className="mb-4">
                  <h3 className="text-sm font-medium text-gray-900 mb-2">核心主题</h3>
                  <div className="flex flex-wrap gap-2">
                    {result.summary.themes.map((theme, i) => (
                      <span
                        key={i}
                        className="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-sm"
                      >
                        {theme}
                      </span>
                    ))}
                  </div>
                </div>

                {/* 关键元素 */}
                <div className="mb-6">
                  <h3 className="text-sm font-medium text-gray-900 mb-2">关键元素</h3>
                  <ul className="space-y-1">
                    {result.summary.keyElements.map((element, i) => (
                      <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                        <span className="w-1 h-1 bg-gray-400 rounded-full" />
                        {element}
                      </li>
                    ))}
                  </ul>
                </div>

                {/* 警告 */}
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
                    className="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium"
                  >
                    感兴趣
                  </button>
                  <button
                    onClick={() => handleFeedback(false)}
                    className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition font-medium"
                  >
                    不感兴趣
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* 反馈历史 */}
          {feedbackHistory.length > 0 && !result && (
            <div className="mt-8">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">最近反馈</h3>
              <div className="space-y-2">
                {feedbackHistory.slice(0, 5).map((item, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
                  >
                    <div className="text-lg">{item.interested ? '👍' : '👎'}</div>
                    <div className="flex-1">
                      <div className="font-medium text-gray-900">{item.title}</div>
                      <div className="text-sm text-gray-500">
                        {new Date(item.timestamp).toLocaleDateString('zh-CN')}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </main>

      <style>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fadeIn {
          animation: fadeIn 0.3s ease-out;
        }
      `}</style>
    </div>
  );
};

export default HomePage;
