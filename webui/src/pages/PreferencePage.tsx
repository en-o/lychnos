import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, BookOpen, Download, Heart, TrendingUp} from 'lucide-react';
import {bookApi} from '../api/book';
import type {UserPreference} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';

const PreferencePage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [preference, setPreference] = useState<UserPreference | null>(null);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    loadPreference();
  }, []);

  const loadPreference = async () => {
    setLoading(true);
    try {
      const response = await bookApi.getUserPreference();
      if (response.success) {
        setPreference(response.data);
      } else {
        toast.error('加载失败');
      }
    } catch (error) {
      console.error('加载偏好失败:', error);
      toast.error('加载失败,请重试');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReport = async () => {
    if (!preference?.annualReport) return;

    setDownloading(true);
    try {
      // Mock: 生成报告内容
      const reportContent = `
【${preference.annualReport.year}年度阅读报告】

总览:
- 分析书籍总数: ${preference.annualReport.totalBooks}
- 感兴趣书籍: ${preference.annualReport.interestedCount}
- 兴趣率: ${((preference.annualReport.interestedCount / preference.annualReport.totalBooks) * 100).toFixed(1)}%

最喜欢的类型:
${preference.annualReport.topGenres.map((g, i) => `${i + 1}. ${g.genre} (${g.count}本)`).join('\n')}

最关注的主题:
${preference.annualReport.topThemes.map((t, i) => `${i + 1}. ${t.theme} (${t.count}次)`).join('\n')}

月度趋势:
${preference.annualReport.monthlyTrend.map(m => `${m.month}月: ${m.count}本`).join('\n')}

亮点回顾:
${preference.annualReport.highlights.map((h, i) => `${i + 1}. ${h}`).join('\n')}

---
由书灯生成 | ${new Date().toLocaleDateString('zh-CN')}
      `.trim();

      // 创建下载
      const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `书灯阅读报告-${preference.annualReport.year}.txt`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast.success('报告已下载');
    } catch (error) {
      console.error('下载失败:', error);
      toast.error('下载失败,请重试');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block w-8 h-8 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
          <p className="text-gray-600 mt-4">加载中...</p>
        </div>
      </div>
    );
  }

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
            <span className="font-semibold text-gray-800">我的偏好</span>
          </div>
        </div>
      </nav>

      {/* 主内容 */}
      <main className="pt-14">
        <div className="max-w-4xl mx-auto px-4 py-8">
          {preference ? (
            <div className="space-y-6">
              {/* 偏好总结 */}
              <div className="bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl p-8 text-white">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center flex-shrink-0">
                    <Heart className="w-6 h-6" />
                  </div>
                  <div className="flex-1">
                    <h2 className="text-2xl font-semibold mb-3">你的阅读画像</h2>
                    <p className="text-lg leading-relaxed opacity-95">
                      {preference.summary}
                    </p>
                  </div>
                </div>
              </div>

              {/* 阅读统计 */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <div className="flex items-center gap-3 mb-2">
                    <BookOpen className="w-5 h-5 text-blue-600" />
                    <h3 className="font-medium text-gray-900">分析总数</h3>
                  </div>
                  <p className="text-3xl font-bold text-gray-900">
                    {preference.readingReport.totalBooks}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">本书籍</p>
                </div>

                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <div className="flex items-center gap-3 mb-2">
                    <Heart className="w-5 h-5 text-red-600" />
                    <h3 className="font-medium text-gray-900">感兴趣</h3>
                  </div>
                  <p className="text-3xl font-bold text-gray-900">
                    {preference.readingReport.interestedBooks}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">
                    占比{' '}
                    {preference.readingReport.totalBooks > 0
                      ? ((preference.readingReport.interestedBooks / preference.readingReport.totalBooks) * 100).toFixed(0)
                      : 0}
                    %
                  </p>
                </div>

                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <div className="flex items-center gap-3 mb-2">
                    <TrendingUp className="w-5 h-5 text-green-600" />
                    <h3 className="font-medium text-gray-900">阅读趋势</h3>
                  </div>
                  <p className="text-2xl font-bold text-gray-900">
                    {preference.readingReport.readingTrend}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">持续成长中</p>
                </div>
              </div>

              {/* 偏好类型 */}
              {preference.readingReport.favoriteGenres.length > 0 && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">偏好类型</h3>
                  <div className="flex flex-wrap gap-3">
                    {preference.readingReport.favoriteGenres.map((genre, i) => (
                      <span
                        key={i}
                        className="px-4 py-2 bg-blue-50 text-blue-700 rounded-lg font-medium"
                      >
                        {genre}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* 关注主题 */}
              {preference.readingReport.favoriteThemes.length > 0 && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">关注主题</h3>
                  <div className="flex flex-wrap gap-2">
                    {preference.readingReport.favoriteThemes.map((theme, i) => (
                      <span
                        key={i}
                        className="px-3 py-1.5 bg-purple-50 text-purple-700 rounded-lg text-sm"
                      >
                        {theme}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* 年度报告 */}
              {preference.annualReport && preference.annualReport.totalBooks > 0 && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <div className="flex items-center justify-between mb-6">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {preference.annualReport.year} 年度报告
                    </h3>
                    <button
                      onClick={handleDownloadReport}
                      disabled={downloading}
                      className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition flex items-center gap-2"
                    >
                      <Download className="w-4 h-4" />
                      {downloading ? '下载中...' : '下载报告'}
                    </button>
                  </div>

                  {/* 亮点 */}
                  <div className="space-y-3 mb-6">
                    {preference.annualReport.highlights.map((highlight, i) => (
                      <div key={i} className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
                        <span className="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm flex-shrink-0">
                          {i + 1}
                        </span>
                        <p className="text-gray-800">{highlight}</p>
                      </div>
                    ))}
                  </div>

                  {/* 类型排行 */}
                  {preference.annualReport.topGenres.length > 0 && (
                    <div className="mb-6">
                      <h4 className="text-sm font-medium text-gray-900 mb-3">类型偏好排行</h4>
                      <div className="space-y-2">
                        {preference.annualReport.topGenres.map((item, i) => (
                          <div key={i} className="flex items-center gap-3">
                            <span className="text-sm font-medium text-gray-600 w-6">
                              #{i + 1}
                            </span>
                            <div className="flex-1 bg-gray-100 rounded-full h-8 overflow-hidden">
                              <div
                                className="bg-gradient-to-r from-blue-500 to-purple-500 h-full flex items-center px-3 text-white text-sm font-medium"
                                style={{
                                  width: `${(item.count / preference.annualReport!.totalBooks) * 100}%`,
                                  minWidth: '60px',
                                }}
                              >
                                {item.genre}
                              </div>
                            </div>
                            <span className="text-sm text-gray-600 w-12 text-right">
                              {item.count}本
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* 月度趋势 */}
                  <div>
                    <h4 className="text-sm font-medium text-gray-900 mb-3">月度趋势</h4>
                    <div className="grid grid-cols-12 gap-1">
                      {preference.annualReport.monthlyTrend.map((item) => {
                        const maxCount = Math.max(...preference.annualReport!.monthlyTrend.map(m => m.count), 1);
                        const height = (item.count / maxCount) * 100;
                        return (
                          <div key={item.month} className="text-center">
                            <div className="h-24 flex items-end justify-center mb-1">
                              <div
                                className="w-full bg-blue-500 rounded-t"
                                style={{ height: `${height}%`, minHeight: item.count > 0 ? '8px' : '0' }}
                                title={`${item.month}月: ${item.count}本`}
                              ></div>
                            </div>
                            <div className="text-xs text-gray-600">{item.month}月</div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-600 mb-4">暂无数据</p>
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
    </div>
  );
};

export default PreferencePage;
