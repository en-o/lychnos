import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type UserDetail, type ThirdPartyBind, type UserPageRequest} from '../api/admin';
import ConfirmDialog from '../components/ConfirmDialog';
import CopyableText from '../components/CopyableText';

function AdminUserPage() {
    const navigate = useNavigate();
    const [users, setUsers] = useState<UserDetail[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null);
    const [bindings, setBindings] = useState<ThirdPartyBind[]>([]);
    const [showBindings, setShowBindings] = useState(false);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [loginName, setLoginName] = useState('');
    const [nickname, setNickname] = useState('');
    const [toggleLoading, setToggleLoading] = useState<number | null>(null);
    const [confirmDialog, setConfirmDialog] = useState<{
        isOpen: boolean;
        title: string;
        message: string;
        type: 'warning' | 'danger';
        onConfirm: () => void;
    }>({
        isOpen: false,
        title: '',
        message: '',
        type: 'warning',
        onConfirm: () => {},
    });

    // 修复：添加搜索条件到依赖项，这样搜索后会自动加载数据
    useEffect(() => {
        loadUsers();
    }, [pageIndex, pageSize, loginName, nickname]);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const params: UserPageRequest = {
                page: { pageIndex, pageSize },
                loginName: loginName || undefined,
                nickname: nickname || undefined,
            };
            const res = await adminApi.user.list(params);
            if (res.success && res.data) {
                setUsers(res.data.rows);
                setTotal(res.data.total);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        } finally {
            setLoading(false);
        }
    };

    // 修复：搜索时重置页码（会触发 useEffect 自动加载数据）
    const handleSearch = () => {
        setPageIndex(1);
    };

    const handleViewBindings = async (user: UserDetail) => {
        try {
            const res = await adminApi.user.thirdPartyBindings(user.id);
            if (res.success) {
                setSelectedUser(user);
                setBindings(res.data);
                setShowBindings(true);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        }
    };

    const handleToggleStatus = async (user: UserDetail) => {
        const isAdmin = user.roles?.includes('ADMIN');
        if (isAdmin) {
            return; // 管理员账户不允许切换状态
        }

        const action = user.status === 1 ? '封禁' : '启用';
        const title = user.status === 1 ? '封禁用户' : '启用用户';
        const message = user.status === 1
            ? `确定要封禁用户 "${user.nickname || user.loginName}" 吗？\n\n封禁后该用户将无法登录系统。`
            : `确定要启用用户 "${user.nickname || user.loginName}" 吗？\n\n启用后该用户将恢复正常访问权限。`;

        setConfirmDialog({
            isOpen: true,
            title,
            message,
            type: user.status === 1 ? 'danger' : 'warning',
            onConfirm: async () => {
                setConfirmDialog({ ...confirmDialog, isOpen: false });
                try {
                    setToggleLoading(user.id);
                    const res = await adminApi.user.toggleStatus(user.id);
                    if (res.success) {
                        await loadUsers(); // 重新加载用户列表
                    }
                } catch (error: any) {
                    // 错误已在拦截器中统一处理
                } finally {
                    setToggleLoading(null);
                }
            },
        });
    };

    const getStatusBadge = (status: number) => {
        switch (status) {
            case 1:
                return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">正常</span>;
            case 3:
                return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">已封禁</span>;
            case 2:
                return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">已注销</span>;
            default:
                return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">未知</span>;
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
                    <h1 className="text-2xl font-bold text-gray-900">用户管理</h1>
                    <button
                        onClick={() => navigate(-1)}
                        className="px-4 py-2 text-gray-600 hover:text-gray-900"
                    >
                        返回
                    </button>
                </div>

                {/* 搜索栏 */}
                <div className="bg-white rounded-lg shadow p-4 mb-4">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <input
                            type="text"
                            placeholder="登录名"
                            value={loginName}
                            onChange={(e) => setLoginName(e.target.value)}
                            className="px-3 py-2 border rounded"
                        />
                        <input
                            type="text"
                            placeholder="昵称"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            className="px-3 py-2 border rounded"
                        />
                        <button
                            onClick={handleSearch}
                            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            搜索
                        </button>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="w-full table-fixed divide-y divide-gray-200">
                        <colgroup>
                            <col className="w-[15%]" />
                            <col className="w-[15%]" />
                            <col className="w-[18%]" />
                            <col className="w-[10%]" />
                            <col className="w-[10%]" />
                            <col className="w-[16%]" />
                            <col className="w-[16%]" />
                        </colgroup>
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">登录名</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">昵称</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">邮箱</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">角色</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">状态</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">注册时间</th>
                                <th className="px-2 py-3 text-left text-xs font-medium text-gray-500 uppercase whitespace-nowrap">操作</th>
                            </tr>
                            </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td className="px-2 py-3 text-sm text-gray-900">
                                    <CopyableText text={user.loginName} maxWidth="100%" />
                                </td>
                                <td className="px-2 py-3 text-sm text-gray-500">
                                    <CopyableText text={user.nickname} maxWidth="100%" />
                                </td>
                                <td className="px-2 py-3 text-sm text-gray-500">
                                    {user.email ? (
                                        <CopyableText text={user.email} maxWidth="100%" />
                                    ) : (
                                        <span>-</span>
                                    )}
                                </td>
                                <td className="px-2 py-3 text-sm whitespace-nowrap">
                                    <div className="flex gap-1 flex-wrap">
                                        {user.roles?.map((role, idx) => (
                                            <span key={idx} className="px-1.5 py-0.5 text-xs rounded-full bg-blue-100 text-blue-800">
                                                {role}
                                            </span>
                                        ))}
                                    </div>
                                </td>
                                <td className="px-2 py-3 text-sm whitespace-nowrap">
                                    {getStatusBadge(user.status)}
                                </td>
                                <td className="px-2 py-3 text-xs text-gray-500 whitespace-nowrap">
                                    {new Date(user.createTime).toLocaleString('zh-CN', {
                                        year: '2-digit',
                                        month: '2-digit',
                                        day: '2-digit',
                                        hour: '2-digit',
                                        minute: '2-digit'
                                    })}
                                </td>
                                <td className="px-2 py-3 whitespace-nowrap text-sm">
                                    <div className="flex gap-1">
                                        <button
                                            onClick={() => handleViewBindings(user)}
                                            className="px-2 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200 text-xs whitespace-nowrap"
                                        >
                                            第三方
                                        </button>
                                        <button
                                            onClick={() => handleToggleStatus(user)}
                                            disabled={user.roles?.includes('ADMIN') || toggleLoading === user.id}
                                            className={`px-2 py-1 rounded text-xs whitespace-nowrap ${
                                                user.roles?.includes('ADMIN')
                                                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                                    : user.status === 1
                                                        ? 'bg-red-100 text-red-700 hover:bg-red-200'
                                                        : 'bg-green-100 text-green-700 hover:bg-green-200'
                                            } disabled:opacity-50`}
                                        >
                                            {toggleLoading === user.id ? '处理中' : user.status === 1 ? '封禁' : '启用'}
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                        </table>

                        {users.length === 0 && (
                            <div className="text-center py-12 text-gray-500">
                                暂无用户数据
                            </div>
                        )}
                    </div>

                {/* 分页 */}
                {total > 0 && (
                    <div className="mt-4 flex items-center justify-between bg-white px-4 py-3 rounded-lg shadow">
                        <div className="flex items-center gap-4">
                            <div className="text-sm text-gray-700">
                                共 {total} 条记录，第 {pageIndex} / {Math.ceil(total / pageSize)} 页
                            </div>
                            <div className="flex items-center gap-2">
                                <label className="text-sm text-gray-700">每页</label>
                                <select
                                    value={pageSize}
                                    onChange={(e) => {
                                        setPageSize(Number(e.target.value));
                                        setPageIndex(1); // 修复：改为 1 而不是 0
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

                {/* 第三方绑定弹窗 */}
                {showBindings && selectedUser && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                        <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-bold">
                                    {selectedUser.nickname} 的第三方绑定
                                </h2>
                                <button
                                    onClick={() => setShowBindings(false)}
                                    className="text-gray-500 hover:text-gray-700"
                                >
                                    ✕
                                </button>
                            </div>

                            {bindings.length === 0 ? (
                                <div className="text-center text-gray-500 py-8">
                                    暂无第三方绑定
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {bindings.map((binding) => (
                                        <div key={binding.userId} className="border rounded-lg p-4">
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center space-x-3">
                                                    {binding.avatarUrl && (
                                                        <img src={binding.avatarUrl} alt="" className="w-10 h-10 rounded-full" />
                                                    )}
                                                    <div>
                                                        <div className="flex items-center gap-2">
                                                            <span className="font-medium">{binding.nickname}</span>
                                                            <span className="px-2 py-0.5 text-xs bg-blue-100 text-blue-700 rounded">
                                                                {binding.providerType}
                                                            </span>
                                                        </div>
                                                        <div className="text-xs text-gray-400 mt-1">
                                                            绑定时间: {new Date(binding.createTime).toLocaleString('zh-CN')}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* 确认对话框 */}
                <ConfirmDialog
                    isOpen={confirmDialog.isOpen}
                    title={confirmDialog.title}
                    message={confirmDialog.message}
                    type={confirmDialog.type}
                    onConfirm={confirmDialog.onConfirm}
                    onCancel={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
                />
            </div>
        </div>
    );
}

export default AdminUserPage;
