import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, Search, FileText} from 'lucide-react';
import {adminApi, type UserAnalysisLog} from '../api/admin';
import {toast} from '../components/ToastContainer';

const AdminLogPage: React.FC = () => {
    const navigate = useNavigate();
    const [logs, setLogs] = useState<UserAnalysisLog[]>([]);
    const [loading, setLoading] = useState(false);

    // 获取默认时间范围：昨天到今天
    const getDefaultTimeRange = () => {
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        const formatDateTime = (date: Date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}T00:00`;
        };

        return {
            startTime: formatDateTime(yesterday),
            endTime: formatDateTime(new Date(today.getTime() + 24 * 60 * 60 * 1000)), // 今天结束
        };
    };

    const defaultTimeRange = getDefaultTimeRange();
    const [queryParams, setQueryParams] = useState({
        startTime: defaultTimeRange.startTime,
        endTime: defaultTimeRange.endTime,
        userName: '',
        exactMatch: false,
        modelSource: undefined as number | undefined,
    });

    // 加载日志
    const loadLogs = async () => {
        setLoading(true);
        try {
            const params: any = {};
            if (queryParams.startTime) {
                params.startTime = queryParams.startTime;
            }
            if (queryParams.endTime) {
                params.endTime = queryParams.endTime;
            }
            if (queryParams.userName) {
                params.userName = queryParams.userName;
                params.exactMatch = queryParams.exactMatch;
            }
            if (queryParams.modelSource !== undefined) {
                params.modelSource = queryParams.modelSource;
            }

            const response = await adminApi.log.query(params);
            if (response.success) {
                setLogs(response.data || []);
                toast.success(`查询成功，共 ${response.data?.length || 0} 条记录`);
            }
        } catch (error: any) {
            console.error('查询日志失败:', error);
            toast.error(error.response?.data?.message || '查询失败，请重试');
        } finally {
            setLoading(false);
        }
    };

    // 页面加载时自动执行查询
    useEffect(() => {
        loadLogs();
    }, []);

    // 重置查询条件
    const resetQuery = () => {
        const defaultTimeRange = getDefaultTimeRange();
        setQueryParams({
            startTime: defaultTimeRange.startTime,
            endTime: defaultTimeRange.endTime,
            userName: '',
            exactMatch: false,
            modelSource: undefined,
        });
    };

    // 格式化时间
    const formatTime = (timeStr: string) => {
        if (!timeStr) return '-';
        return new Date(timeStr).toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
        });
    };

    // 获取模型来源文本
    const getModelSourceText = (source: number) => {
        switch (source) {
            case 0:
                return '官方';
            case 1:
                return '私人';
            case 2:
                return '公开';
            default:
                return '-';
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* 顶部导航栏 */}
            <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
                <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => navigate('/sys-manage')}
                            className="p-1.5 hover:bg-gray-100 rounded-lg transition"
                        >
                            <ArrowLeft className="w-5 h-5 text-gray-600" />
                        </button>
                        <FileText className="w-5 h-5 text-gray-600" />
                        <span className="font-semibold text-gray-800">日志查询</span>
                    </div>
                </div>
            </nav>

            {/* 主内容 */}
            <main className="pt-14">
                <div className="max-w-7xl mx-auto px-4 py-8">
                    {/* 查询表单 */}
                    <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
                        <h2 className="text-lg font-semibold text-gray-900 mb-4">查询条件</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    开始时间
                                </label>
                                <input
                                    type="datetime-local"
                                    value={queryParams.startTime}
                                    onChange={(e) => setQueryParams({...queryParams, startTime: e.target.value})}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    结束时间
                                </label>
                                <input
                                    type="datetime-local"
                                    value={queryParams.endTime}
                                    onChange={(e) => setQueryParams({...queryParams, endTime: e.target.value})}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    用户名
                                </label>
                                <input
                                    type="text"
                                    value={queryParams.userName}
                                    onChange={(e) => setQueryParams({...queryParams, userName: e.target.value})}
                                    placeholder="输入用户名"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    模型来源
                                </label>
                                <select
                                    value={queryParams.modelSource === undefined ? '' : queryParams.modelSource}
                                    onChange={(e) => setQueryParams({...queryParams, modelSource: e.target.value === '' ? undefined : Number(e.target.value)})}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                >
                                    <option value="">全部</option>
                                    <option value="0">官方</option>
                                    <option value="1">私人</option>
                                    <option value="2">公开</option>
                                </select>
                            </div>
                            <div className="flex items-end">
                                <label className="flex items-center gap-2 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={queryParams.exactMatch}
                                        onChange={(e) => setQueryParams({...queryParams, exactMatch: e.target.checked})}
                                        className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                    />
                                    <span className="text-sm text-gray-700">精确匹配用户名</span>
                                </label>
                            </div>
                        </div>
                        <div className="flex gap-3">
                            <button
                                onClick={loadLogs}
                                disabled={loading}
                                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2 disabled:opacity-50"
                            >
                                <Search className="w-4 h-4" />
                                {loading ? '查询中...' : '查询'}
                            </button>
                            <button
                                onClick={resetQuery}
                                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                            >
                                重置
                            </button>
                        </div>
                        <p className="text-xs text-gray-500 mt-3">
                            💡 提示：默认查询昨天到今天的记录，最多返回200条记录
                        </p>
                    </div>

                    {/* 日志列表 */}
                    <div className="bg-white rounded-lg border border-gray-200">
                        <div className="px-6 py-4 border-b border-gray-200">
                            <h2 className="text-lg font-semibold text-gray-900">
                                日志记录 {logs.length > 0 && `(${logs.length})`}
                            </h2>
                        </div>
                        <div className="overflow-x-auto">
                            {logs.length === 0 ? (
                                <div className="text-center py-12">
                                    <FileText className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                                    <p className="text-gray-500">暂无日志记录</p>
                                    <p className="text-sm text-gray-400 mt-1">请调整查询条件后重试</p>
                                </div>
                            ) : (
                                <table className="w-full">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                调用时间
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                用户名
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                调用IP
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                模型名称
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                模型类型
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                模型来源
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                用途
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                书籍标题
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                状态
                                            </th>
                                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                使用已有数据
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {logs.map((log, index) => (
                                            <tr key={index} className="hover:bg-gray-50">
                                                <td className="px-4 py-3 text-sm text-gray-900 whitespace-nowrap">
                                                    {formatTime(log.createTime)}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    <span
                                                        className="cursor-help"
                                                        title={`用户ID: ${log.userId}`}
                                                    >
                                                        {log.userName || '-'}
                                                    </span>
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    {log.callIp || '-'}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    <span
                                                        className="cursor-help"
                                                        title={`模型ID: ${log.modelId}\n模型厂商: ${log.modelVendor || '-'}`}
                                                    >
                                                        {log.modelName || '-'}
                                                    </span>
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    {log.modelType || '-'}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    {getModelSourceText(log.modelSource)}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    {log.usageType || '-'}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900 max-w-xs truncate">
                                                    <span title={log.bookTitle}>
                                                        {log.bookTitle || '-'}
                                                    </span>
                                                </td>
                                                <td className="px-4 py-3 text-sm">
                                                    {log.success ? (
                                                        <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">
                                                            成功
                                                        </span>
                                                    ) : (
                                                        <span
                                                            className="px-2 py-1 bg-red-100 text-red-800 rounded-full text-xs cursor-help"
                                                            title={log.errorMessage || '未知错误'}
                                                        >
                                                            失败
                                                        </span>
                                                    )}
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-900">
                                                    {log.useExistingData ? '是' : '否'}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default AdminLogPage;
