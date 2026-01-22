import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type OAuthConfigDetail, type OAuthConfigUpdate} from '../api/admin';
import {toast} from '../components/ToastContainer';

function AdminOAuthConfigPage() {
    const navigate = useNavigate();
    const [configs, setConfigs] = useState<OAuthConfigDetail[]>([]);
    const [loading, setLoading] = useState(true);
    const [editingConfig, setEditingConfig] = useState<OAuthConfigDetail | null>(null);
    const [showEditDialog, setShowEditDialog] = useState(false);
    const [formData, setFormData] = useState<OAuthConfigUpdate>({
        id: 0,
        clientId: '',
        clientSecret: '',
        webCallbackUrl: '',
    });

    useEffect(() => {
        loadConfigs();
    }, []);

    const loadConfigs = async () => {
        try {
            setLoading(true);
            const res = await adminApi.oauth.list();
            if (res.success) {
                setConfigs(res.data);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        } finally {
            setLoading(false);
        }
    };

    const handleToggle = async (id: number) => {
        try {
            const res = await adminApi.oauth.toggle(id);
            if (res.success) {
                toast.success(res.message || '操作成功');
                loadConfigs();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        }
    };

    const handleEdit = (config: OAuthConfigDetail) => {
        setEditingConfig(config);
        setFormData({
            id: config.id,
            clientId: config.clientId,
            clientSecret: '',
            webCallbackUrl: config.webCallbackUrl || '',
        });
        setShowEditDialog(true);
    };

    const handleSave = async () => {
        try {
            const res = await adminApi.oauth.update(formData);
            if (res.success) {
                toast.success('保存成功');
                setShowEditDialog(false);
                loadConfigs();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        }
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
                    <h1 className="text-2xl font-bold text-gray-900">OAuth配置管理</h1>
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
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">平台</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Client ID</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">回调地址</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">排序</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {configs.map((config) => (
                                <tr key={config.id}>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center">
                                            {config.iconUrl && (
                                                <img src={config.iconUrl} alt="" className="w-6 h-6 mr-2" />
                                            )}
                                            <span className="text-sm font-medium text-gray-900">{config.providerName}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-500">{config.clientId}</td>
                                    <td className="px-6 py-4 text-sm text-gray-500">{config.webCallbackUrl || '-'}</td>
                                    <td className="px-6 py-4 text-sm text-gray-500">{config.sortOrder}</td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 text-xs rounded-full ${config.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                                            {config.enabled ? '已启用' : '已停用'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <button
                                            onClick={() => handleEdit(config)}
                                            className="px-3 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200 mr-2"
                                        >
                                            编辑
                                        </button>
                                        <button
                                            onClick={() => handleToggle(config.id)}
                                            className={`px-3 py-1 rounded ${config.enabled ? 'bg-red-100 text-red-700 hover:bg-red-200' : 'bg-green-100 text-green-700 hover:bg-green-200'}`}
                                        >
                                            {config.enabled ? '停用' : '启用'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* 编辑对话框 */}
                {showEditDialog && editingConfig && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                        <div className="bg-white rounded-lg p-6 max-w-3xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-bold">编辑 {editingConfig.providerName} 配置</h2>
                                <button
                                    onClick={() => setShowEditDialog(false)}
                                    className="text-gray-500 hover:text-gray-700"
                                >
                                    ✕
                                </button>
                            </div>

                            <div className="space-y-4">
                                {/* Client ID */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Client ID
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.clientId}
                                        onChange={(e) => setFormData({...formData, clientId: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                    />
                                </div>

                                {/* Client Secret */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Client Secret（留空则不修改）
                                    </label>
                                    <input
                                        type="password"
                                        value={formData.clientSecret}
                                        onChange={(e) => setFormData({...formData, clientSecret: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="留空则不修改"
                                    />
                                </div>

                                {/* Web回调地址 */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Web回调地址前缀
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.webCallbackUrl}
                                        onChange={(e) => setFormData({...formData, webCallbackUrl: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="http://localhost:3000/lychnos"
                                    />

                                    {/* 快捷选项 */}
                                    <div className="mt-2 flex flex-wrap gap-2">
                                        <span className="text-xs text-gray-600 self-center">快捷选项：</span>
                                        <button
                                            type="button"
                                            onClick={() => setFormData({...formData, webCallbackUrl: 'http://localhost:3000/lychnos'})}
                                            className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 rounded border border-gray-300"
                                        >
                                            http://localhost:3000/lychnos
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setFormData({...formData, webCallbackUrl: 'https://lychnos.tannn.cn'})}
                                            className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 rounded border border-gray-300"
                                        >
                                            https://lychnos.tannn.cn
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setFormData({...formData, webCallbackUrl: ''})}
                                            className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 rounded border border-gray-300"
                                        >
                                            清空（相对路径）
                                        </button>
                                    </div>

                                    <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded text-sm text-gray-700">
                                        <p className="font-semibold mb-2">📝 配置说明：</p>
                                        <ul className="space-y-1 list-disc list-inside">
                                            <li>此字段只需填写<strong>域名+路径前缀</strong>，后端会自动拼接 <code className="bg-gray-200 px-1">#/oauth/callback</code></li>
                                            <li>末尾的斜杠会被自动移除</li>
                                            <li>可以为空，表示使用相对路径</li>
                                        </ul>
                                        <p className="font-semibold mt-3 mb-1">示例：</p>
                                        <ul className="space-y-1 text-xs">
                                            <li>• <code className="bg-gray-200 px-1">http://localhost:3000/lychnos</code> → <code className="bg-gray-200 px-1">http://localhost:3000/lychnos#/oauth/callback?token=xxx</code></li>
                                            <li>• <code className="bg-gray-200 px-1">http://localhost:3000</code> → <code className="bg-gray-200 px-1">http://localhost:3000#/oauth/callback?token=xxx</code></li>
                                            <li>• <code className="bg-gray-200 px-1">https://example.com</code> → <code className="bg-gray-200 px-1">https://example.com#/oauth/callback?token=xxx</code></li>
                                            <li>• 留空 → <code className="bg-gray-200 px-1">#/oauth/callback?token=xxx</code></li>
                                        </ul>
                                    </div>
                                </div>

                                {/* 按钮 */}
                                <div className="flex justify-end space-x-3 pt-4">
                                    <button
                                        onClick={() => setShowEditDialog(false)}
                                        className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                                    >
                                        取消
                                    </button>
                                    <button
                                        onClick={handleSave}
                                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                                    >
                                        保存
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminOAuthConfigPage;
