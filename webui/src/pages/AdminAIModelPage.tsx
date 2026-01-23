import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type AIModelDetail, type AIModelPageRequest} from '../api/admin';
import {toast} from '../components/ToastContainer';

function AdminAIModelPage() {
    const navigate = useNavigate();
    const [models, setModels] = useState<AIModelDetail[]>([]);
    const [loading, setLoading] = useState(true);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [loginName, setLoginName] = useState('');
    const [model, setModel] = useState('');
    const [type, setType] = useState(''); // 新增：type 检索条件

    // 修复：添加搜索条件到依赖项，这样搜索后会自动加载数据
    useEffect(() => {
        loadModels();
    }, [pageIndex, pageSize, loginName, model, type]);

    const loadModels = async () => {
        try {
            setLoading(true);
            const params: AIModelPageRequest = {
                page: { pageIndex, pageSize },
                loginName: loginName || undefined,
                model: model || undefined,
                type: type || undefined, // 新增：添加 type 参数
            };
            const res = await adminApi.aiModel.list(params);
            if (res.success && res.data) {
                setModels(res.data.rows);
                setTotal(res.data.total);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理
        } finally {
            setLoading(false);
        }
    };

    // 修复：搜索时重置页码（会触发 useEffect 自动加载数据）
    const handleSearch = () => {
        setPageIndex(1);
    };

    const handleSetOfficial = async (model: AIModelDetail) => {
        // 检查模型是否可用
        if (!model.enabled) {
            toast.error('模型状态非可用，不允许设置为官方');
            return;
        }

        if (!window.confirm(`确定要将 "${model.name}" 设置为官方模型吗？`)) {
            return;
        }

        try {
            const res = await adminApi.aiModel.setOfficial(model.id);
            if (res.success) {
                toast.success('已设置为官方模型');
                loadModels();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理
        }
    };

    const handleSetPrivate = async (model: AIModelDetail) => {
        if (!window.confirm(`确定要将 "${model.name}" 设置为私人模型吗？`)) {
            return;
        }

        try {
            const res = await adminApi.aiModel.setPrivate(model.id);
            if (res.success) {
                toast.success('已设置为私人模型');
                loadModels();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理
        }
    };

    const getShareTypeName = (share: number) => {
        switch (share) {
            case 0:
                return '官方';
            case 1:
                return '私人';
            case 2:
                return '公开';
            default:
                return '未知';
        }
    };

    const getShareTypeColor = (share: number) => {
        switch (share) {
            case 0:
                return 'bg-blue-100 text-blue-800';
            case 1:
                return 'bg-gray-100 text-gray-800';
            case 2:
                return 'bg-green-100 text-green-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    const getModelTypeName = (type: string) => {
        return type === 'TEXT' ? '文本' : type === 'IMAGE' ? '图片' : type;
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-gray-600">加载中...</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 py-8">
                <div className="mb-6 flex items-center justify-between">
                    <h1 className="text-2xl font-bold text-gray-900">AI模型管理</h1>
                    <button
                        onClick={() => navigate(-1)}
                        className="px-4 py-2 text-gray-600 hover:text-gray-900"
                    >
                        返回
                    </button>
                </div>

                {/* 搜索栏 */}
                <div className="bg-white rounded-lg shadow p-4 mb-4">
                    <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
                        <input
                            type="text"
                            placeholder="模型归属(登录名)"
                            value={loginName}
                            onChange={(e) => setLoginName(e.target.value)}
                            className="px-3 py-2 border rounded"
                        />
                        <input
                            type="text"
                            placeholder="模型名"
                            value={model}
                            onChange={(e) => setModel(e.target.value)}
                            className="px-3 py-2 border rounded"
                        />
                        <select
                            value={type}
                            onChange={(e) => setType(e.target.value)}
                            className="px-3 py-2 border rounded"
                        >
                            <option value="">全部类型</option>
                            <option value="TEXT">文本</option>
                            <option value="IMAGE">图片</option>
                        </select>
                        <button
                            onClick={handleSearch}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            搜索
                        </button>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模型归属(登录名)</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">配置名</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模型名称</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">厂家</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">分享状态</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">可用状态</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {models.map((model) => (
                            <tr key={model.id}>
                                <td className="px-6 py-4 text-sm text-gray-500">{model.loginName}</td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <span className="text-sm font-medium text-gray-900">{model.name}</span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500">{model.model}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{model.factory}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{getModelTypeName(model.type)}</td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 text-xs rounded-full ${getShareTypeColor(model.share)}`}>
                                            {getShareTypeName(model.share)}
                                        </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 text-xs rounded-full ${model.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                                            {model.enabled ? '可用' : '禁用'}
                                        </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                                    {model.share !== 0 && (
                                        <button
                                            onClick={() => handleSetOfficial(model)}
                                            disabled={!model.enabled}
                                            className={`px-3 py-1 rounded mr-2 ${
                                                !model.enabled
                                                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                                    : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                                            }`}
                                            title={!model.enabled ? '模型状态非可用，不允许设置为官方' : '设置为官方模型'}
                                        >
                                            设为官方
                                        </button>
                                    )}
                                    {model.share !== 1 && (
                                        <button
                                            onClick={() => handleSetPrivate(model)}
                                            className="px-3 py-1 rounded bg-gray-100 text-gray-700 hover:bg-gray-200"
                                        >
                                            设为私人
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {models.length === 0 && (
                        <div className="text-center py-12 text-gray-500">
                            暂无AI模型数据
                        </div>
                    )}
                </div>

                {/* 分页 */}
                {total > 0 && (
                    <div className="mt-4 flex items-center justify-between bg-white px-4 py-3 rounded-lg shadow">
                        <div className="flex items-center gap-4">
                            <div className="text-sm text-gray-700">
                                共 {total} 条记录,第 {pageIndex} / {Math.ceil(total / pageSize)} 页
                            </div>
                            <div className="flex items-center gap-2">
                                <label className="text-sm text-gray-700">每页</label>
                                <select
                                    value={pageSize}
                                    onChange={(e) => {
                                        setPageSize(Number(e.target.value));
                                        setPageIndex(1);
                                    }}
                                    className="px-2 py-1 border rounded"
                                >
                                    <option value={1}>1</option>
                                    <option value={10}>10</option>
                                    <option value={20}>20</option>
                                    <option value={30}>30</option>
                                </select>
                                <label className="text-sm text-gray-700">条</label>
                            </div>
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setPageIndex(prev => Math.max(1, prev - 1))}
                                disabled={pageIndex === 1}
                                className="px-3 py-1 border rounded disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                上一页
                            </button>
                            <button
                                onClick={() => setPageIndex(prev => prev + 1)}
                                disabled={pageIndex >= Math.ceil(total / pageSize)}
                                className="px-3 py-1 border rounded disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                下一页
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminAIModelPage;
