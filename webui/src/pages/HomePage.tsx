import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Brain, ChevronDown, Heart, History, Key, LogOut, Search, UserCircle} from 'lucide-react';
import {bookApi} from '../api/book';
import type {AnalysisHistory, BookAnalysis, BookExtract, BookRecommendItem} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';
import ConfirmDialog from '../components/ConfirmDialog';
import ImagePreview from '../components/ImagePreview';
import {getImageUrl} from '../utils/imageUrl';
import {BOOK_ALREADY_ANALYZED} from '../constants/errorCodes';

// 装饰主题类型
type DecorationTheme = 'daily' | 'christmas' | 'spring-festival';

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [bookTitle, setBookTitle] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<BookAnalysis | null>(null);
  const [extractedBooks, setExtractedBooks] = useState<BookExtract[]>([]);
  const [feedbackHistory, setFeedbackHistory] = useState<AnalysisHistory[]>([]);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showLoginConfirm, setShowLoginConfirm] = useState(false);
  const [selectedHistoryItem, setSelectedHistoryItem] = useState<AnalysisHistory | null>(null);
  const [showBackConfirm, setShowBackConfirm] = useState(false);
  const [quickBooks, setQuickBooks] = useState<BookRecommendItem[]>([]);
  const [decorationTheme, setDecorationTheme] = useState<DecorationTheme>('daily');
  const [imageError, setImageError] = useState<Record<string, boolean>>({});
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [searchHistory, setSearchHistory] = useState<string[]>([]);
  const [showHistory, setShowHistory] = useState(false);

  // 检查是否已登录
  const token = localStorage.getItem('token');
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');

  // 加载搜索历史
  React.useEffect(() => {
    const history = localStorage.getItem('searchHistory');
    if (history) {
      try {
        setSearchHistory(JSON.parse(history));
      } catch (e) {
        console.error('加载搜索历史失败:', e);
      }
    }
  }, []);

  // 保存搜索历史
  const saveSearchHistory = (title: string) => {
    const trimmedTitle = title.trim();
    if (!trimmedTitle) return;

    setSearchHistory(prev => {
      // 移除重复项并添加到开头
      const newHistory = [trimmedTitle, ...prev.filter(item => item !== trimmedTitle)];
      // 限制最多20条
      const limitedHistory = newHistory.slice(0, 20);
      // 保存到 localStorage
      localStorage.setItem('searchHistory', JSON.stringify(limitedHistory));
      return limitedHistory;
    });
  };

  // 清除搜索历史
  const clearSearchHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem('searchHistory');
    setShowHistory(false);
  };

  // 加载快速推荐书籍
  React.useEffect(() => {
    const loadQuickBooks = async () => {
      try {
        const response = await bookApi.getQuickBooks();
        if (response.success && response.data) {
          setQuickBooks(response.data);
        }
      } catch (error) {
        console.error('获取快速推荐失败:', error);
        // 降级使用默认值
        setQuickBooks([
          { id: '1001', title: '三体' },
          { id: '1002', title: '活着' },
          { id: '1003', title: '解忧杂货店' },
          { id: '1004', title: '人类简史' },
        ]);
      }
    };
    loadQuickBooks();
  }, []);

  // 加载历史记录
  React.useEffect(() => {
    if (token) {
      loadFeedbackHistory();
    }
  }, [token]);

  const loadFeedbackHistory = async () => {
    try {
      const response = await bookApi.getFeedbackHistory();
      if (response.success && response.data) {
        setFeedbackHistory(response.data);
      }
    } catch (error) {
      console.error('加载反馈历史失败:', error);
    }
  };

  const handleSearch = async (title = bookTitle, isRecommended = false) => {
    if (!title.trim()) {
      toast.warning('请输入书名');
      return;
    }

    // 保存到搜索历史
    saveSearchHistory(title);
    setShowHistory(false);

    setLoading(true);
    setResult(null);
    setExtractedBooks([]);

    try {
      // 1. 未登录用户：只能查看推荐书籍
      if (!token) {
        if (isRecommended) {
          // 推荐书籍，使用统一接口查询
          const response = await bookApi.queryBookAnalysis(title);
          if (response.success && response.data) {
            setResult(response.data);
            toast.info('未登录用户只能查看推荐书籍的分析结果');
          }
        } else {
          // 非推荐书籍，提示需要登录
          setShowLoginConfirm(true);
        }
        return;
      }

      // 2. 已登录用户 - 推荐书籍：直接检查并分析
      if (isRecommended) {
        const checkResponse = await bookApi.queryBookAnalysis(title);
        if (checkResponse.success && checkResponse.data) {
          // 已分析过，跳转到历史记录页面
          toast.info('该书籍已经分析过，正在跳转到历史记录...');
          navigate(`/history?search=${encodeURIComponent(title)}`);
          return;
        }

        // 未分析过，直接分析（推荐书籍书名准确，不需要提取）
        await analyzeBook({ title, author: '', analyzed: false });
        return;
      }

      // 3. 已登录用户 - 输入框输入：先提取书籍信息
      const extractResponse = await bookApi.extractBooks(title);

      if (extractResponse.success && extractResponse.data && extractResponse.data.length > 0) {
        const books = extractResponse.data;

        // 无论提取到几本书，都显示列表让用户确认选择
        setExtractedBooks(books);
        toast.success(`识别到 ${books.length} 本书籍，请选择要分析的书籍`);
      } else {
        toast.warning('未能识别到书籍信息，请尝试更明确的书名');
      }
    } catch (error: any) {
      console.error('搜索失败:', error);
      // 错误提示已在request拦截器中统一处理
    } finally {
      setLoading(false);
    }
  };

  // 分析书籍
  const analyzeBook = async (book: BookExtract) => {
    setLoading(true);
    setExtractedBooks([]);

    try {
      const response = await bookApi.analyzeBook({
        title: book.title,
        author: book.author
      });

      if (response.success) {
        setResult(response.data);
        toast.success('分析完成！');
      }
    } catch (error: any) {
      console.error('分析失败:', error);
      if (error?.code === BOOK_ALREADY_ANALYZED) {
        toast.info('该书籍已经分析过，正在跳转到历史记录...');
        navigate(`/history?search=${encodeURIComponent(book.title)}`);
      }
    } finally {
      setLoading(false);
    }
  };

  // 处理书籍选择
  const handleBookSelect = async (book: BookExtract) => {
    // 检查是否已分析
    setLoading(true);
    try {
      const checkResponse = await bookApi.queryBookAnalysis(book.title);
      if (checkResponse.success && checkResponse.data) {
        // 已分析过，跳转到历史记录页面
        toast.info('该书籍已经分析过，正在跳转到历史记录...');
        navigate(`/history?search=${encodeURIComponent(book.title)}`);
      } else {
        // 未分析过，执行分析
        await analyzeBook(book);
      }
    } catch (error) {
      console.error('检查失败:', error);
      // 如果检查失败，直接尝试分析
      await analyzeBook(book);
    } finally {
      setLoading(false);
    }
  };

  const handleFeedback = async (interested: boolean) => {
    if (!result) return;

    try {
      const response = await bookApi.submitFeedback({
        bookAnalyseId: result.id,
        bookTitle: result.title,
        interested,
        reason: ''
      });

      if (response.success) {
        toast.success('反馈已提交!');

        // 重新加载反馈历史
        await loadFeedbackHistory();
        setResult(null);
        setBookTitle('');
      }
    } catch (error) {
      console.error('提交失败:', error);
      // 错误提示已在request拦截器中统一处理，不需要重复提示
    }
  };

  /**
   * 数据库的数据快速检索
   * @param title 书名
   */
  const handleQuickSearch = (title: string) => {
    setBookTitle(title);
    handleSearch(title, true); // 标记为推荐书籍
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

  const handleBackToSearch = () => {
    setShowBackConfirm(true);
  };

  const confirmBackToSearch = () => {
    setResult(null);
    setBookTitle('');
    setShowBackConfirm(false);
  };

  // 切换主题
  const toggleTheme = () => {
    const themes: DecorationTheme[] = ['daily', 'christmas', 'spring-festival'];
    const currentIndex = themes.indexOf(decorationTheme);
    const nextIndex = (currentIndex + 1) % themes.length;
    setDecorationTheme(themes[nextIndex]);
  };

  // 点击外部关闭下拉菜单
  React.useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (showUserMenu && !target.closest('.user-menu-container')) {
        setShowUserMenu(false);
      }
      if (showHistory && !target.closest('.search-container')) {
        setShowHistory(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showUserMenu, showHistory]);

  return (
    <div className="min-h-screen bg-white">
      {/* 顶部导航栏 - ChatGPT 风格 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          {/* Logo - 点击切换主题 */}
          <button
            onClick={toggleTheme}
            className="flex items-center gap-2 hover:opacity-80 transition group"
            title="点击切换主题"
          >
            <Logo className="w-5 h-5 transition-transform group-hover:scale-110" />
            <span className="font-semibold text-gray-800">书灯</span>
          </button>

          {/* 用户区域 */}
          <div className="flex items-center gap-3">
            {token ? (
              <div className="relative user-menu-container">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition"
                >
                  <div className="w-7 h-7 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white text-sm font-medium">
                    {(() => {
                      const str = (userInfo.nickname || userInfo.loginName || 'tan').toString();
                      const firstChar = str.charAt(0);
                      // 判断首字符是否为中文（包括常用汉字）
                      const isChinese = /[\u4e00-\u9fff]/.test(firstChar);
                      const displayText = isChinese ?
                          str.substring(0, 1).toUpperCase() :
                          str.substring(0, 3).toUpperCase();
                      return displayText;
                    })()}
                  </div>
                  <ChevronDown className="w-4 h-4 text-gray-600" />
                </button>

                {/* 下拉菜单 */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="font-medium text-gray-900">{userInfo.nickname || userInfo.loginName}</p>
                      <p className="text-sm text-gray-500">{userInfo.email || userInfo.loginName}</p>
                    </div>

                    <button
                      onClick={() => handleMenuClick('/profile')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <UserCircle className="w-4 h-4" />
                      个人资料
                    </button>

                    <button
                      onClick={() => handleMenuClick('/history')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <History className="w-4 h-4" />
                      我的历史
                    </button>

                    <button
                      onClick={() => handleMenuClick('/preference')}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3"
                    >
                      <Heart className="w-4 h-4" />
                      我的偏好
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
              {/* 装饰区域 */}
              <div className="relative inline-block mb-8">
                {/* 日常主题 */}
                {decorationTheme === 'daily' && (
                  <>
                    {/* 不对称的书本和装饰 */}
                    <div className="absolute -top-8 -left-4 text-3xl opacity-60 animate-float-slow" style={{transform: 'rotate(-15deg)'}}>
                      📚
                    </div>
                    <div className="absolute -top-6 right-8 text-2xl opacity-50 animate-float-medium">💡</div>
                    <div className="absolute -bottom-2 -right-6 text-xl opacity-40 animate-float-fast" style={{transform: 'rotate(20deg)'}}>✨</div>
                  </>
                )}

                {/* 圣诞主题 */}
                {decorationTheme === 'christmas' && (
                  <>
                    {/* 彩灯线 */}
                    <svg className="absolute -top-12 left-1/2 -translate-x-1/2 w-[400px] h-16" style={{overflow: 'visible'}}>
                      {/* 电线 */}
                      <path
                        d="M 20,20 Q 80,12 140,20 T 260,20 T 380,20"
                        stroke="#9CA3AF"
                        strokeWidth="1.5"
                        fill="none"
                        className="opacity-60"
                      />

                      {/* 彩灯泡 */}
                      <g className="holiday-light">
                        <line x1="60" y1="16" x2="60" y2="26" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="60" cy="31" rx="6" ry="8" fill="#EF4444" className="light-glow-red" />
                      </g>

                      <g className="holiday-light" style={{animationDelay: '0.3s'}}>
                        <line x1="120" y1="18" x2="120" y2="30" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="120" cy="35" rx="6" ry="8" fill="#FBBF24" className="light-glow-yellow" />
                      </g>

                      <g className="holiday-light" style={{animationDelay: '0.6s'}}>
                        <line x1="180" y1="20" x2="180" y2="28" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="180" cy="33" rx="6" ry="8" fill="#10B981" className="light-glow-green" />
                      </g>

                      <g className="holiday-light" style={{animationDelay: '0.9s'}}>
                        <line x1="240" y1="18" x2="240" y2="29" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="240" cy="34" rx="6" ry="8" fill="#3B82F6" className="light-glow-blue" />
                      </g>

                      <g className="holiday-light" style={{animationDelay: '1.2s'}}>
                        <line x1="300" y1="20" x2="300" y2="27" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="300" cy="32" rx="6" ry="8" fill="#8B5CF6" className="light-glow-purple" />
                      </g>

                      <g className="holiday-light" style={{animationDelay: '1.5s'}}>
                        <line x1="340" y1="17" x2="340" y2="29" stroke="#9CA3AF" strokeWidth="1" />
                        <ellipse cx="340" cy="34" rx="6" ry="8" fill="#EC4899" className="light-glow-pink" />
                      </g>
                    </svg>

                    {/* 小装饰 */}
                    <div className="absolute -right-6 top-0 text-xl opacity-50 animate-float-slow">❄️</div>
                    <div className="absolute -left-6 top-2 text-lg opacity-40 animate-float-medium">✨</div>
                  </>
                )}

                {/* 春节主题 */}
                {decorationTheme === 'spring-festival' && (
                  <>
                    {/* 不对称的灯笼和装饰 */}
                    <div className="absolute -top-14 -left-8 animate-swing-left">
                      <div className="text-3xl">🏮</div>
                    </div>
                    <div className="absolute -top-10 right-12 animate-swing-right" style={{animationDelay: '0.3s'}}>
                      <div className="text-2xl">🏮</div>
                    </div>

                    {/* 烟花和其他装饰 - 不对称布局 */}
                    <div className="absolute -left-12 top-4 text-xl opacity-50 animate-float-slow">🎆</div>
                    <div className="absolute -right-4 -top-4 text-2xl opacity-60 animate-float-medium" style={{transform: 'rotate(15deg)'}}>🎇</div>

                    {/* 金币和红包 */}
                    <div className="absolute left-2 -bottom-4 text-lg opacity-45 animate-float-fast">🪙</div>
                    <div className="absolute -right-8 bottom-2 text-xl opacity-55 animate-float-slow" style={{transform: 'rotate(-10deg)'}}>🧧</div>
                  </>
                )}

                {/* 文字内容 */}
                <p className="text-gray-600 text-lg">
                  在翻开书之前，先点一盏灯
                </p>
              </div>

              {/* 搜索框 */}
              <div className="max-w-2xl mx-auto mb-6">
                <div className="relative search-container">
                  <input
                    type="text"
                    value={bookTitle}
                    onChange={(e) => setBookTitle(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    onFocus={() => searchHistory.length > 0 && setShowHistory(true)}
                    placeholder={token ? "输入书名,开始分析..." : "输入书名搜索，或点击下方推荐书籍查看分析"}
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

                  {/* 搜索历史下拉 */}
                  {showHistory && searchHistory.length > 0 && (
                    <div className="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-lg z-50 max-h-80 overflow-y-auto">
                      <div className="flex items-center justify-between px-4 py-2 border-b border-gray-100">
                        <span className="text-sm font-medium text-gray-700">搜索历史</span>
                        <button
                          onClick={clearSearchHistory}
                          className="text-xs text-red-600 hover:text-red-700"
                        >
                          清除历史
                        </button>
                      </div>
                      <div className="py-1">
                        {searchHistory.map((item, index) => (
                          <button
                            key={index}
                            onClick={() => {
                              setBookTitle(item);
                              setShowHistory(false);
                              handleSearch(item);
                            }}
                            className="w-full px-4 py-2.5 text-left hover:bg-gray-50 flex items-center gap-3 group"
                          >
                            <History className="w-4 h-4 text-gray-400 group-hover:text-gray-600" />
                            <span className="text-gray-700 group-hover:text-gray-900">{item}</span>
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* 隐私协议说明 */}
              <div className="max-w-2xl mx-auto mb-6">
                {token ? (
                  <div className="px-4 py-2.5 bg-blue-50 border border-blue-100 rounded-lg">
                    <p className="text-xs text-blue-700 text-center leading-relaxed">
                      <span className="font-medium">隐私说明：</span>
                      使用 AI 分析功能时，您的书籍分析结果（非个人信息）将用于改进服务质量。
                      我们承诺：<span className="font-medium">① 绝不使用您的 API Key</span>；
                      <span className="font-medium">② 仅共享书籍分析内容</span>；
                      <span className="font-medium">③ 您也将受益于其他用户的分析成果</span>，加快相同书籍的分析速度。
                    </p>
                  </div>
                ) : (
                  <div className="px-4 py-2.5 bg-amber-50 border border-amber-100 rounded-lg">
                    <p className="text-xs text-amber-700 text-center leading-relaxed">
                      <span className="font-medium">未登录提示：</span>
                      当前未登录，您可以<span className="font-medium">点击下方推荐书籍查看分析结果</span>，无需登录。
                      若想<span className="font-medium">分析更多书籍并保存偏好</span>，请先
                      <button
                        onClick={handleLogin}
                        className="text-blue-600 hover:text-blue-700 underline font-medium mx-1"
                      >
                        登录
                      </button>
                      使用完整功能。
                    </p>
                  </div>
                )}
              </div>

              {/* 快速选择 */}
              <div className="flex items-center justify-center gap-2 flex-wrap">
                <span className="text-sm text-gray-500">
                  {token ? '试试:' : '推荐（点击即可查看）:'}
                </span>
                {quickBooks.map((book) => (
                  <button
                    key={book.id}
                    onClick={() => handleQuickSearch(book.title)}
                    disabled={loading}
                    className="px-3 py-1.5 text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition disabled:opacity-50"
                  >
                    {book.title}
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
                onClick={handleBackToSearch}
                className="mb-6 text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1"
              >
                ← 返回分析
              </button>

              {/* 书籍信息卡片 */}
              <div className="bg-white border border-gray-200 rounded-xl p-6 mb-4">
                {result.posterUrl && result.posterUrl !== '' ? (
                  <div className="mb-6">
                    {!imageError[result.id] ? (
                      <img
                        src={getImageUrl(result.posterUrl)}
                        alt={result.title}
                        className="w-full h-64 object-fill rounded-lg cursor-pointer hover:opacity-90 transition"
                        onError={() => setImageError(prev => ({ ...prev, [result.id]: true }))}
                        onClick={() => setPreviewImage(getImageUrl(result.posterUrl))}
                      />
                    ) : (
                      <div className="w-full h-64 bg-gray-100 rounded-lg flex items-center justify-center">
                        <div className="text-center text-gray-400">
                          <svg className="w-16 h-16 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                          <p className="text-sm">图片加载失败</p>
                        </div>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="mb-6 p-6 bg-amber-50 border border-amber-200 rounded-lg">
                    <div className="flex items-start gap-3">
                      <svg className="w-6 h-6 text-amber-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div className="flex-1">
                        <h4 className="text-sm font-medium text-amber-900 mb-1">图览信息生成失败</h4>
                        <p className="text-sm text-amber-700 mb-2">
                          该书籍分析暂无配图，可能是生图模型配置或网络问题导致。您可以：
                        </p>
                        <ul className="text-sm text-amber-600 space-y-1 list-disc list-inside">
                          <li>重新搜索该书名，系统将尝试重新生成图片</li>
                          <li>或直接查看下方的文字分析内容</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                )}

                <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                  {result.title}
                </h2>

                <div className="flex gap-2 mb-4">
                  <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                    {result.genre}
                  </span>
                  <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                    {result.tone}
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
                    {result.themes.map((theme, i) => (
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
                    {result.keyElements.map((element, i) => (
                      <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                        <span className="w-1 h-1 bg-gray-400 rounded-full" />
                        {element}
                      </li>
                    ))}
                  </ul>
                </div>

                {/* 反馈按钮 */}
                {token ? (
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
                ) : (
                  <div className="space-y-3">
                    <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                      <p className="text-sm text-blue-700 text-center">
                        想要分析更多书籍并保存偏好？请先登录
                      </p>
                    </div>
                    <button
                      onClick={handleLogin}
                      className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                    >
                      登录以使用完整功能
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 反馈历史 */}
          {feedbackHistory.length > 0 && !result && token && (
            <div className="mt-8">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">最近分析</h3>
                <button
                  onClick={() => navigate('/history')}
                  className="text-sm text-blue-600 hover:text-blue-700 transition"
                >
                  查看全部 →
                </button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {feedbackHistory.slice(0, 10).map((item) => (
                  <div
                    key={item.id}
                    onClick={() => setSelectedHistoryItem(item)}
                    className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md hover:border-blue-300 transition cursor-pointer"
                  >
                    <div className="flex items-start gap-3">
                      <div className="flex-shrink-0">
                        {item.interested ? (
                          <span className="inline-flex items-center justify-center w-8 h-8 bg-green-100 text-green-600 rounded-full">
                            👍
                          </span>
                        ) : (
                          <span className="inline-flex items-center justify-center w-8 h-8 bg-gray-100 text-gray-600 rounded-full">
                            👎
                          </span>
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-medium text-gray-900 mb-1 truncate">
                          {item.title}
                        </h4>
                        {item.analysisData && (
                          <>
                            <div className="flex flex-wrap gap-1 mb-2">
                              <span className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded text-xs">
                                {item.analysisData.genre}
                              </span>
                              <span className="px-2 py-0.5 bg-purple-50 text-purple-700 rounded text-xs">
                                {item.analysisData.tone}
                              </span>
                            </div>
                          </>
                        )}
                        <div className="text-xs text-gray-500">
                          {new Date(item.createTime).toLocaleDateString('zh-CN', {
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </div>
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

        /* Google风格彩灯闪烁动画 */
        @keyframes holiday-twinkle {
          0%, 100% {
            opacity: 1;
            filter: brightness(1);
          }
          50% {
            opacity: 0.6;
            filter: brightness(1.4);
          }
        }

        .holiday-light {
          animation: holiday-twinkle 2s ease-in-out infinite;
        }

        /* 各种颜色的发光效果 */
        .light-glow-red {
          filter: drop-shadow(0 0 3px rgba(239, 68, 68, 0.6));
        }
        .light-glow-yellow {
          filter: drop-shadow(0 0 3px rgba(251, 191, 36, 0.6));
        }
        .light-glow-green {
          filter: drop-shadow(0 0 3px rgba(16, 185, 129, 0.6));
        }
        .light-glow-blue {
          filter: drop-shadow(0 0 3px rgba(59, 130, 246, 0.6));
        }
        .light-glow-purple {
          filter: drop-shadow(0 0 3px rgba(139, 92, 246, 0.6));
        }
        .light-glow-pink {
          filter: drop-shadow(0 0 3px rgba(236, 72, 153, 0.6));
        }

        /* 漂浮动画 */
        @keyframes float-slow {
          0%, 100% {
            transform: translateY(0) rotate(0deg);
          }
          50% {
            transform: translateY(-10px) rotate(5deg);
          }
        }

        @keyframes float-medium {
          0%, 100% {
            transform: translateY(0) rotate(0deg);
          }
          50% {
            transform: translateY(-8px) rotate(-3deg);
          }
        }

        @keyframes float-fast {
          0%, 100% {
            transform: translateY(0) scale(1);
          }
          50% {
            transform: translateY(-12px) scale(1.1);
          }
        }

        .animate-float-slow {
          animation: float-slow 4s ease-in-out infinite;
        }

        .animate-float-medium {
          animation: float-medium 3.5s ease-in-out infinite;
        }

        .animate-float-fast {
          animation: float-fast 3s ease-in-out infinite;
        }

        /* 灯笼摇摆动画 */
        @keyframes swing-left {
          0%, 100% {
            transform: rotate(-5deg);
          }
          50% {
            transform: rotate(5deg);
          }
        }

        @keyframes swing-right {
          0%, 100% {
            transform: rotate(5deg);
          }
          50% {
            transform: rotate(-5deg);
          }
        }

        .animate-swing-left {
          transform-origin: top center;
          animation: swing-left 2s ease-in-out infinite;
        }

        .animate-swing-right {
          transform-origin: top center;
          animation: swing-right 2s ease-in-out infinite;
        }
      `}</style>

      {/* 登录确认对话框 */}
      {showLoginConfirm && (
        <ConfirmDialog
          message="需要登录才能使用分析功能，是否前往登录？"
          onConfirm={() => {
            setShowLoginConfirm(false);
            navigate('/login?redirect=/');
          }}
          onCancel={() => setShowLoginConfirm(false)}
        />
      )}

      {/* 返回确认对话框 */}
      {showBackConfirm && (
        <ConfirmDialog
          message="你还没有提交反馈，确定要返回吗？"
          onConfirm={confirmBackToSearch}
          onCancel={() => setShowBackConfirm(false)}
        />
      )}

      {/* 历史详情弹窗 */}
      {selectedHistoryItem && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[10000] p-4">
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <h2 className="text-2xl font-semibold text-gray-900">
                  {selectedHistoryItem.title}
                </h2>
                {selectedHistoryItem.interested ? (
                  <span className="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm">
                    <span>👍</span>
                    感兴趣
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-sm">
                    <span>👎</span>
                    不感兴趣
                  </span>
                )}
              </div>
              <button
                onClick={() => setSelectedHistoryItem(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="space-y-4">
              {selectedHistoryItem.analysisData && (
                <>
                  <div className="flex gap-2">
                    <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                      {selectedHistoryItem.analysisData.genre}
                    </span>
                    <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                      {selectedHistoryItem.analysisData.tone}
                    </span>
                  </div>

                  {selectedHistoryItem.analysisData.posterUrl && (
                    <div>
                      {!imageError[selectedHistoryItem.id] ? (
                        <img
                          src={getImageUrl(selectedHistoryItem.analysisData.posterUrl)}
                          alt={selectedHistoryItem.title}
                          className="w-full h-48 object-fill rounded-lg cursor-pointer hover:opacity-90 transition"
                          onError={() => setImageError(prev => ({ ...prev, [selectedHistoryItem.id]: true }))}
                          onClick={() => setPreviewImage(getImageUrl(selectedHistoryItem.analysisData.posterUrl))}
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
                    <p className="text-gray-800">{selectedHistoryItem.analysisData.recommendation}</p>
                  </div>

                  <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-2">核心主题</h3>
                    <div className="flex flex-wrap gap-2">
                      {selectedHistoryItem.analysisData.themes.map((theme, i) => (
                        <span key={i} className="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-sm">
                          {theme}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-2">关键元素</h3>
                    <ul className="space-y-1">
                      {selectedHistoryItem.analysisData.keyElements.map((element, i) => (
                        <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                          <span className="w-1 h-1 bg-gray-400 rounded-full" />
                          {element}
                        </li>
                      ))}
                    </ul>
                  </div>
                </>
              )}

              {!selectedHistoryItem.analysisData && (
                <div className="text-center py-8 text-gray-500">
                  该书籍暂无分析数据
                </div>
              )}

              <div className="pt-4 border-t border-gray-200 text-sm text-gray-500">
                分析时间: {new Date(selectedHistoryItem.createTime).toLocaleDateString('zh-CN', {
                  year: 'numeric',
                  month: '2-digit',
                  day: '2-digit',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 书籍选择弹窗 */}
      {extractedBooks.length > 0 && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[10000] p-4">
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 rounded-t-xl">
              <div className="flex items-center justify-between">
                <h3 className="text-xl font-semibold text-gray-900">
                  识别到 {extractedBooks.length} 本书籍
                </h3>
                <button
                  onClick={() => {
                    setExtractedBooks([]);
                    setBookTitle('');
                  }}
                  className="text-gray-400 hover:text-gray-600 transition"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <p className="text-sm text-gray-600 mt-2">
                请选择要分析的书籍
              </p>
            </div>

            <div className="p-6 space-y-3">
              {extractedBooks.map((book, index) => {
                const isAlreadyAnalyzed = book.sourceType === 'ALREADY_ANALYZED';

                return (
                  <button
                    key={index}
                    onClick={() => handleBookSelect(book)}
                    disabled={loading}
                    className={`w-full p-4 border-2 rounded-lg transition text-left disabled:opacity-50 disabled:cursor-not-allowed group ${
                      isAlreadyAnalyzed
                        ? 'border-green-300 bg-green-50 hover:border-green-500 hover:bg-green-100'
                        : 'border-gray-200 hover:border-blue-500 hover:bg-blue-50'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <h4 className={`font-medium ${
                            isAlreadyAnalyzed
                              ? 'text-green-900 group-hover:text-green-700'
                              : 'text-gray-900 group-hover:text-blue-700'
                          }`}>
                            {book.title}
                          </h4>
                          {book.sourceLabel && (
                            <span className={`inline-block px-2 py-0.5 text-xs rounded ${
                              book.sourceType === 'ALREADY_ANALYZED'
                                ? 'bg-green-200 text-green-800 font-medium'
                                : book.sourceType === 'USER_INPUT'
                                ? 'bg-blue-100 text-blue-700'
                                : book.sourceType === 'SIMILAR'
                                ? 'bg-purple-100 text-purple-700'
                                : 'bg-amber-100 text-amber-700'
                            }`}>
                              {book.sourceLabel}
                            </span>
                          )}
                        </div>
                        {book.author && (
                          <p className={`text-sm mb-2 ${
                            isAlreadyAnalyzed ? 'text-green-700' : 'text-gray-600'
                          }`}>
                            作者：{book.author}
                          </p>
                        )}
                        {isAlreadyAnalyzed && (
                          <div className="mt-2 p-2 bg-green-100 border border-green-200 rounded text-xs text-green-800">
                            💡 该书籍已分析过，点击可直接查看分析结果
                          </div>
                        )}
                      </div>
                      <div className="ml-4 flex-shrink-0">
                        <svg className={`w-5 h-5 transition ${
                          isAlreadyAnalyzed
                            ? 'text-green-500 group-hover:text-green-600'
                            : 'text-gray-400 group-hover:text-blue-500'
                        }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>

            {loading && (
              <div className="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 rounded-b-xl">
                <div className="flex items-center justify-center gap-2 text-sm text-gray-600">
                  <div className="w-4 h-4 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin" />
                  <span>正在处理...</span>
                </div>
              </div>
            )}
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

      {/* 全屏加载提示 */}
      {loading && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[10001]">
          <div className="bg-white rounded-xl p-8 flex flex-col items-center gap-4 max-w-md">
            <div className="w-16 h-16 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
            <p className="text-gray-700 text-lg font-medium">正在分析书籍...</p>
            <div className="text-center space-y-2">
              <p className="text-gray-500 text-sm">AI 正在深度分析书籍内容并生成封面图</p>
              <p className="text-gray-400 text-xs">预计需要 1-10 分钟[具体看模型处理能力]，请耐心等待</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default HomePage;
