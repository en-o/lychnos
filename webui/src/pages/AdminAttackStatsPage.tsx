import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, RefreshCw, Trash2, AlertTriangle} from 'lucide-react';
import {adminApi, type AttackStats, type AttackRecord} from '../api/admin';
import {toast} from '../components/ToastContainer';

function AdminAttackStatsPage() {
    const navigate = useNavigate();
    const [stats, setStats] = useState<AttackStats | null>(null);
    const [loading, setLoading] = useState(false);
    const [limit, setLimit] = useState(10);
    const [threshold, setThreshold] = useState(20);
    const [highFrequencyAttackers, setHighFrequencyAttackers] = useState<AttackRecord[]>([]);

    const loadStats = async () => {
        setLoading(true);
        try {
            const res = await adminApi.log.getAttackStats(limit);
            if (res.success) {
                setStats(res.data);
            } else {
                toast.error(res.message || '加载失败');
            }
        } catch (error) {
            toast.error('加载攻击统计失败');
        } finally {
            setLoading(false);
        }
    };

    const loadHighFrequencyAttackers = async () => {
        try {
            const res = await adminApi.log.getHighFrequencyAttackers(threshold);
            if (res.success) {
                setHighFrequencyAttackers(res.data);
            }
        } catch (error) {
            toast.error('加载高频攻击者失败');
        }
    };

    const handleRemoveIp = async (ip: string) => {
        if (!confirm(`确定要移除 IP ${ip} 的攻击统计吗？`)) return;

        try {
            const res = await adminApi.log.removeIpStats(ip);
            if (res.success) {
                toast.success(res.data);
                loadStats();
                loadHighFrequencyAttackers();
            } else {
                toast.error(res.message || '操作失败');
            }
        } catch (error) {
            toast.error('移除失败');
        }
    };

    const handleClearAll = async () => {
        if (!confirm('确定要清空所有攻击统计数据吗？此操作不可恢复！')) return;

        try {
            const res = await adminApi.log.clearAttackStats();
            if (res.success) {
                toast.success(res.data);
                loadStats();
                setHighFrequencyAttackers([]);
            } else {
                toast.error(res.message || '操作失败');
            }
        } catch (error) {
            toast.error('清空失败');
        }
    };

    useEffect(() => {
        loadStats();
        loadHighFrequencyAttackers();
    }, []);

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-6xl mx-auto px-4 py-8">
                <div className="mb-6 flex items-center justify-between">
                    <h1 className="text-2xl font-bold text-gray-900">攻击统计</h1>
                    <button
                        onClick={() => navigate('/sys-manage')}
                        className="px-4 py-2 text-gray-600 hover:text-gray-900 flex items-center gap-2"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        返回系统管理
                    </button>
                </div>

                {/* 统计概览 */}
                {stats && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="text-sm text-gray-600 mb-1">总攻击IP数</div>
                            <div className="text-3xl font-bold text-gray-900">{stats.totalIpCount}</div>
                        </div>
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="text-sm text-gray-600 mb-1">总攻击次数</div>
                            <div className="text-3xl font-bold text-red-600">{stats.totalAttackCount}</div>
                        </div>
                    </div>
                )}

                {/* 操作栏 */}
                <div className="bg-white rounded-lg shadow p-4 mb-6">
                    <div className="flex flex-wrap items-center gap-4">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-600">显示TOP</label>
                            <input
                                type="number"
                                value={limit}
                                onChange={(e) => setLimit(Number(e.target.value))}
                                className="w-20 px-2 py-1 border rounded"
                                min="0"
                            />
                        </div>
                        <button
                            onClick={loadStats}
                            disabled={loading}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                        >
                            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                            刷新
                        </button>
                        <button
                            onClick={handleClearAll}
                            className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 flex items-center gap-2"
                        >
                            <Trash2 className="w-4 h-4" />
                            清空所有
                        </button>
                    </div>
                </div>

                {/* TOP攻击者列表 */}
                <div className="bg-white rounded-lg shadow mb-6">
                    <div className="p-4 border-b">
                        <h2 className="text-lg font-semibold text-gray-900">TOP攻击者</h2>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">排名</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP地址</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">攻击次数</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                                {stats?.topAttackers.map((record, index) => (
                                    <tr key={record.ip} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 text-sm text-gray-900">{index + 1}</td>
                                        <td className="px-6 py-4 text-sm font-mono text-gray-900">{record.ip}</td>
                                        <td className="px-6 py-4 text-sm text-red-600 font-semibold">{record.count}</td>
                                        <td className="px-6 py-4 text-sm">
                                            <button
                                                onClick={() => handleRemoveIp(record.ip)}
                                                className="text-red-600 hover:text-red-800"
                                            >
                                                移除
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {stats?.topAttackers.length === 0 && (
                            <div className="text-center py-8 text-gray-500">暂无攻击记录</div>
                        )}
                    </div>
                </div>

                {/* 高频攻击者 */}
                <div className="bg-white rounded-lg shadow">
                    <div className="p-4 border-b flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <AlertTriangle className="w-5 h-5 text-orange-600" />
                            <h2 className="text-lg font-semibold text-gray-900">高频攻击者</h2>
                        </div>
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-600">阈值</label>
                            <input
                                type="number"
                                value={threshold}
                                onChange={(e) => setThreshold(Number(e.target.value))}
                                className="w-20 px-2 py-1 border rounded"
                                min="1"
                            />
                            <button
                                onClick={loadHighFrequencyAttackers}
                                className="px-3 py-1 bg-orange-600 text-white rounded hover:bg-orange-700 text-sm"
                            >
                                查询
                            </button>
                        </div>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP地址</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">攻击次数</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                                {highFrequencyAttackers.map((record) => (
                                    <tr key={record.ip} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 text-sm font-mono text-gray-900">{record.ip}</td>
                                        <td className="px-6 py-4 text-sm text-orange-600 font-semibold">{record.count}</td>
                                        <td className="px-6 py-4 text-sm">
                                            <button
                                                onClick={() => handleRemoveIp(record.ip)}
                                                className="text-red-600 hover:text-red-800"
                                            >
                                                移除
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {highFrequencyAttackers.length === 0 && (
                            <div className="text-center py-8 text-gray-500">暂无高频攻击者</div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AdminAttackStatsPage;
