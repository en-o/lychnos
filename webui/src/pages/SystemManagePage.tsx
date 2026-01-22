import {useNavigate} from 'react-router-dom';
import {Settings, Users, ArrowLeft} from 'lucide-react';

function SystemManagePage() {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-4xl mx-auto px-4 py-8">
                <div className="mb-6 flex items-center justify-between">
                    <h1 className="text-2xl font-bold text-gray-900">系统管理</h1>
                    <button
                        onClick={() => navigate('/')}
                        className="px-4 py-2 text-gray-600 hover:text-gray-900 flex items-center gap-2"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        返回首页
                    </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* OAuth配置管理 */}
                    <button
                        onClick={() => navigate('/sys-manage/oauth-config')}
                        className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-left"
                    >
                        <div className="flex items-center gap-4 mb-3">
                            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                                <Settings className="w-6 h-6 text-blue-600" />
                            </div>
                            <h2 className="text-xl font-semibold text-gray-900">OAuth配置</h2>
                        </div>
                        <p className="text-gray-600">管理第三方登录配置，包括启用/停用、修改回调地址等</p>
                    </button>

                    {/* 用户管理 */}
                    <button
                        onClick={() => navigate('/sys-manage/users')}
                        className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-left"
                    >
                        <div className="flex items-center gap-4 mb-3">
                            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                                <Users className="w-6 h-6 text-green-600" />
                            </div>
                            <h2 className="text-xl font-semibold text-gray-900">用户管理</h2>
                        </div>
                        <p className="text-gray-600">查看所有用户信息、角色和第三方账号绑定情况</p>
                    </button>
                </div>
            </div>
        </div>
    );
}

export default SystemManagePage;
