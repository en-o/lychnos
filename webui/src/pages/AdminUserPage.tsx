import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {adminApi, type UserDetail, type ThirdPartyBind} from '../api/admin';

function AdminUserPage() {
    const navigate = useNavigate();
    const [users, setUsers] = useState<UserDetail[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null);
    const [bindings, setBindings] = useState<ThirdPartyBind[]>([]);
    const [showBindings, setShowBindings] = useState(false);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const res = await adminApi.user.list();
            if (res.success) {
                setUsers(res.data);
            }
        } catch (error: any) {
            // 错误已在拦截器中统一处理，这里只需要捕获异常
        } finally {
            setLoading(false);
        }
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

                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">登录名</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">昵称</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">邮箱</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">角色</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">创建时间</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {users.map((user) => (
                                <tr key={user.id}>
                                    <td className="px-6 py-4 text-sm text-gray-900">{user.id}</td>
                                    <td className="px-6 py-4 text-sm text-gray-900">{user.loginName}</td>
                                    <td className="px-6 py-4 text-sm text-gray-500">{user.nickname}</td>
                                    <td className="px-6 py-4 text-sm text-gray-500">{user.email || '-'}</td>
                                    <td className="px-6 py-4 text-sm">
                                        <div className="flex gap-1">
                                            {user.roles?.map((role, idx) => (
                                                <span key={idx} className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">
                                                    {role}
                                                </span>
                                            ))}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-500">
                                        {new Date(user.createTime).toLocaleString('zh-CN')}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <button
                                            onClick={() => handleViewBindings(user)}
                                            className="px-3 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200"
                                        >
                                            查看绑定
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

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
            </div>
        </div>
    );
}

export default AdminUserPage;
