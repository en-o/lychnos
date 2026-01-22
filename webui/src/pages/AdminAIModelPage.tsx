import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type AIModelDetail} from '../api/admin';
import {toast} from '../components/ToastContainer';

function AdminAIModelPage() {
    const navigate = useNavigate();
    const [models, setModels] = useState<AIModelDetail[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadModels();
    }, []);

    const loadModels = async () => {
        try {
            setLoading(true);
            const res = await adminApi.aiModel.list();
            if (res.success) {
                setModels(res.data);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理
        } finally {
            setLoading(false);
        }
    };

    const handleSetOfficial = async (model: AIModelDetail) => {
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

                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模型名称</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模型</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">厂家</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">分享状态</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">启用状态</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {models.map((model) => (
                                <tr key={model.id}>
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
                                            {model.enabled ? '已启用' : '已停用'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        {model.share !== 0 && (
                                            <button
                                                onClick={() => handleSetOfficial(model)}
                                                className="px-3 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200 mr-2"
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
            </div>
        </div>
    );
}

export default AdminAIModelPage;
