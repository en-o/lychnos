import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type OAuthConfigDetail, type OAuthConfigUpdate} from '../api/admin';
import {toast} from '../components/ToastContainer';

// OAuth 平台默认配置
const OAUTH_DEFAULTS: Record<string, Partial<OAuthConfigUpdate>> = {
    GITHUB: {
        authorizeUrl: 'https://github.com/login/oauth/authorize',
        tokenUrl: 'https://github.com/login/oauth/access_token',
        userInfoUrl: 'https://api.github.com/user',
        scope: 'read:user user:email',
        iconUrl: 'https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png',
    },
    LINUXDO: {
        authorizeUrl: 'https://connect.linux.do/oauth2/authorize',
        tokenUrl: 'https://connect.linux.do/oauth2/token',
        userInfoUrl: 'https://connect.linux.do/api/user',
        scope: 'read',
        iconUrl: 'https://linux.do/uploads/default/optimized/4X/c/c/d/ccd8c210609d498cbeb3d5201d4c259348447562_2_32x32.png',
    },
    QQ: {
        authorizeUrl: 'https://graph.qq.com/oauth2.0/authorize',
        tokenUrl: 'https://graph.qq.com/oauth2.0/token',
        userInfoUrl: 'https://graph.qq.com/user/get_user_info',
        scope: 'get_user_info',
        iconUrl: '',
    },
    WECHAT: {
        authorizeUrl: 'https://open.weixin.qq.com/connect/qrconnect',
        tokenUrl: 'https://api.weixin.qq.com/sns/oauth2/access_token',
        userInfoUrl: 'https://api.weixin.qq.com/sns/userinfo',
        scope: 'snsapi_login',
        iconUrl: '',
    },
};

function AdminOAuthConfigPage() {
    const navigate = useNavigate();
    const [configs, setConfigs] = useState<OAuthConfigDetail[]>([]);
    const [loading, setLoading] = useState(true);
    const [editingConfig, setEditingConfig] = useState<OAuthConfigDetail | null>(null);
    const [showEditDialog, setShowEditDialog] = useState(false);
    const [isCreateMode, setIsCreateMode] = useState(false);
    const [showCallbackHelp, setShowCallbackHelp] = useState(false);
    const [formData, setFormData] = useState<OAuthConfigUpdate>({
        id: 0,
        providerType: '',
        clientId: '',
        clientSecret: '',
        authorizeUrl: '',
        tokenUrl: '',
        userInfoUrl: '',
        scope: '',
        iconUrl: '',
        sortOrder: 0,
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

    const handleCreate = () => {
        setIsCreateMode(true);
        setEditingConfig(null);
        const defaultProvider = 'GITHUB';
        const defaults = OAUTH_DEFAULTS[defaultProvider] || {};
        setFormData({
            id: 0,
            providerType: defaultProvider,
            clientId: '',
            clientSecret: '',
            authorizeUrl: defaults.authorizeUrl || '',
            tokenUrl: defaults.tokenUrl || '',
            userInfoUrl: defaults.userInfoUrl || '',
            scope: defaults.scope || '',
            iconUrl: defaults.iconUrl || '',
            sortOrder: 0,
            webCallbackUrl: '',
        });
        setShowEditDialog(true);
    };

    const handleProviderTypeChange = (providerType: string) => {
        const defaults = OAUTH_DEFAULTS[providerType] || {};
        setFormData({
            ...formData,
            providerType,
            authorizeUrl: defaults.authorizeUrl || '',
            tokenUrl: defaults.tokenUrl || '',
            userInfoUrl: defaults.userInfoUrl || '',
            scope: defaults.scope || '',
            iconUrl: defaults.iconUrl || '',
        });
    };

    const handleEdit = (config: OAuthConfigDetail) => {
        setIsCreateMode(false);
        setEditingConfig(config);
        setFormData({
            id: config.id,
            providerType: config.providerType,
            clientId: config.clientId,
            clientSecret: '',
            authorizeUrl: config.authorizeUrl,
            tokenUrl: config.tokenUrl,
            userInfoUrl: config.userInfoUrl,
            scope: config.scope,
            iconUrl: config.iconUrl,
            sortOrder: config.sortOrder,
            webCallbackUrl: config.webCallbackUrl || '',
        });
        setShowEditDialog(true);
    };

    const handleSave = async () => {
        // 前端验证必填字段
        if (isCreateMode) {
            if (!formData.providerType) {
                toast.error('请选择平台类型');
                return;
            }
            if (!formData.clientSecret?.trim()) {
                toast.error('请输入 Client Secret');
                return;
            }
        }

        // 新增和编辑都需要验证的必填字段
        if (!formData.clientId?.trim()) {
            toast.error('请输入 Client ID');
            return;
        }
        if (!formData.authorizeUrl?.trim()) {
            toast.error('请输入授权端点');
            return;
        }
        if (!formData.tokenUrl?.trim()) {
            toast.error('请输入 Token 端点');
            return;
        }
        if (!formData.userInfoUrl?.trim()) {
            toast.error('请输入用户信息端点');
            return;
        }
        if (!formData.webCallbackUrl?.trim()) {
            toast.error('请输入 Web 回调地址前缀');
            return;
        }

        try {
            const res = isCreateMode
                ? await adminApi.oauth.create(formData)
                : await adminApi.oauth.update(formData);
            if (res.success) {
                toast.success(isCreateMode ? '新增成功' : '保存成功');
                setShowEditDialog(false);
                loadConfigs();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        }
    };

    const handleSortChange = async (id: number, newSort: number) => {
        try {
            const res = await adminApi.oauth.updateSort(id, newSort);
            if (res.success) {
                toast.success('排序更新成功');
                loadConfigs();
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        }
    };

    const handleDelete = async (config: OAuthConfigDetail) => {
        // 检查是否为启用状态
        if (config.enabled) {
            toast.error('启用状态的配置不允许删除，请先停用');
            return;
        }

        if (!window.confirm(`确定要删除 ${config.providerName} 的配置吗？`)) {
            return;
        }

        try {
            const res = await adminApi.oauth.delete(config.id);
            if (res.success) {
                toast.success('删除成功');
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
                    <div className="flex gap-3">
                        <button
                            onClick={() => handleCreate()}
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                        >
                            新增配置
                        </button>
                        <button
                            onClick={() => navigate(-1)}
                            className="px-4 py-2 text-gray-600 hover:text-gray-900"
                        >
                            返回
                        </button>
                    </div>
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
                                    <td className="px-6 py-4 text-sm">
                                        <input
                                            type="number"
                                            value={config.sortOrder}
                                            onChange={(e) => handleSortChange(config.id, parseInt(e.target.value) || 0)}
                                            className="w-16 px-2 py-1 border border-gray-300 rounded text-center"
                                            min="0"
                                        />
                                    </td>
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
                                            className={`px-3 py-1 rounded mr-2 ${config.enabled ? 'bg-red-100 text-red-700 hover:bg-red-200' : 'bg-green-100 text-green-700 hover:bg-green-200'}`}
                                        >
                                            {config.enabled ? '停用' : '启用'}
                                        </button>
                                        <button
                                            onClick={() => handleDelete(config)}
                                            disabled={config.enabled}
                                            className={`px-3 py-1 rounded ${
                                                config.enabled
                                                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                                    : 'bg-red-100 text-red-700 hover:bg-red-200'
                                            }`}
                                            title={config.enabled ? '请先停用后再删除' : '删除配置'}
                                        >
                                            删除
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* 编辑/新增对话框 */}
                {showEditDialog && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                        <div className="bg-white rounded-lg p-6 max-w-3xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-bold">
                                    {isCreateMode ? '新增 OAuth 配置' : `编辑 ${editingConfig?.providerName} 配置`}
                                </h2>
                                <button
                                    onClick={() => setShowEditDialog(false)}
                                    className="text-gray-500 hover:text-gray-700"
                                >
                                    ✕
                                </button>
                            </div>

                            <div className="space-y-4">
                                {/* 平台类型 - 仅新增时显示 */}
                                {isCreateMode && (
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            平台类型 <span className="text-red-500">*</span>
                                        </label>
                                        <select
                                            value={formData.providerType}
                                            onChange={(e) => handleProviderTypeChange(e.target.value)}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        >
                                            <option value="GITHUB">GitHub</option>
                                            <option value="LINUXDO">LinuxDo</option>
                                            <option value="QQ">QQ（支持中）</option>
                                            <option value="WECHAT">微信（支持中）</option>
                                        </select>
                                        <p className="mt-1 text-xs text-gray-500">切换平台类型会自动填充默认端点配置</p>
                                    </div>
                                )}

                                {/* Client ID */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Client ID <span className="text-red-500">*</span>
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
                                        Client Secret {isCreateMode && <span className="text-red-500">*</span>}
                                        {!isCreateMode && <span className="text-gray-500 text-xs">（留空则不修改）</span>}
                                    </label>
                                    <input
                                        type="password"
                                        value={formData.clientSecret}
                                        onChange={(e) => setFormData({...formData, clientSecret: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder={isCreateMode ? '请输入 Client Secret' : '留空则不修改'}
                                    />
                                </div>

                                {/* 授权端点 */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        授权端点 (Authorize URL) <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.authorizeUrl}
                                        onChange={(e) => setFormData({...formData, authorizeUrl: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="https://example.com/oauth/authorize"
                                    />
                                </div>

                                {/* Token端点 */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Token端点 (Token URL) <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.tokenUrl}
                                        onChange={(e) => setFormData({...formData, tokenUrl: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="https://example.com/oauth/token"
                                    />
                                </div>

                                {/* 用户信息端点 */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        用户信息端点 (User Info URL) <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.userInfoUrl}
                                        onChange={(e) => setFormData({...formData, userInfoUrl: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="https://example.com/api/user"
                                    />
                                </div>

                                {/* Web回调地址 */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Web回调地址前缀 <span className="text-red-500">*</span>
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
                                            onClick={() => setFormData({...formData, webCallbackUrl: 'http://localhost:1250'})}
                                            className="px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 rounded border border-gray-300"
                                        >
                                            http://localhost:1250
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

                                    <div className="mt-2 border border-blue-200 rounded overflow-hidden">
                                        <button
                                            type="button"
                                            onClick={() => setShowCallbackHelp(!showCallbackHelp)}
                                            className="w-full px-3 py-2 bg-blue-50 text-left flex items-center justify-between hover:bg-blue-100 transition"
                                        >
                                            <span className="font-semibold text-sm text-gray-700">📝 配置说明</span>
                                            <span className="text-gray-500 text-xs">
                                                {showCallbackHelp ? '▲ 收起' : '▼ 展开'}
                                            </span>
                                        </button>
                                        {showCallbackHelp && (
                                            <div className="p-3 bg-blue-50 text-sm text-gray-700 border-t border-blue-200">
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
                                        )}
                                    </div>
                                </div>

                                {/* 平台图标URL */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        平台图标URL (Icon URL)
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.iconUrl}
                                        onChange={(e) => setFormData({...formData, iconUrl: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="https://example.com/icon.png"
                                    />
                                    <p className="mt-1 text-xs text-gray-500">可选字段，留空时登录页面将使用平台名称作为图标</p>
                                </div>

                                {/* Scope */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        权限范围 (Scope)
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.scope}
                                        onChange={(e) => setFormData({...formData, scope: e.target.value})}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md"
                                        placeholder="read:user user:email"
                                    />
                                    <div className="mt-2 p-2 bg-gray-50 border border-gray-200 rounded text-xs text-gray-600">
                                        <p className="font-semibold mb-1">📖 Scope 文档参考：</p>
                                        <ul className="space-y-1">
                                            <li>• GitHub: <a href="https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/scopes-for-oauth-apps" target="_blank" className="text-blue-600 hover:underline">OAuth Scopes</a></li>
                                            <li>• LinuxDo: <a href="https://connect.linux.do/dash/sso" target="_blank" className="text-blue-600 hover:underline">OAuth2 文档</a></li>
                                            <li>• QQ: <a href="https://wiki.connect.qq.com/oauth2-0%e7%ae%80%e4%bb%8b" target="_blank" className="text-blue-600 hover:underline">QQ互联文档</a></li>
                                            <li>• 微信: <a href="https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html" target="_blank" className="text-blue-600 hover:underline">微信开放平台</a></li>
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
