import {useNavigate} from 'react-router-dom';
import {ArrowLeft, Brain, FileText, Settings, Shield, Users} from 'lucide-react';

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

                    {/* AI模型管理 */}
                    <button
                        onClick={() => navigate('/sys-manage/ai-model')}
                        className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-left"
                    >
                        <div className="flex items-center gap-4 mb-3">
                            <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                                <Brain className="w-6 h-6 text-purple-600" />
                            </div>
                            <h2 className="text-xl font-semibold text-gray-900">AI模型管理</h2>
                        </div>
                        <p className="text-gray-600">管理AI模型的分享状态，设置官方或私人模型</p>
                    </button>

                    {/* 日志查询 */}
                    <button
                        onClick={() => navigate('/sys-manage/logs')}
                        className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-left"
                    >
                        <div className="flex items-center gap-4 mb-3">
                            <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
                                <FileText className="w-6 h-6 text-orange-600" />
                            </div>
                            <h2 className="text-xl font-semibold text-gray-900">日志查询</h2>
                        </div>
                        <p className="text-gray-600">查询用户分析日志，支持时间范围和用户名筛选</p>
                    </button>

                    {/* 攻击统计 */}
                    <button
                        onClick={() => navigate('/sys-manage/attack-stats')}
                        className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-left"
                    >
                        <div className="flex items-center gap-4 mb-3">
                            <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                                <Shield className="w-6 h-6 text-red-600" />
                            </div>
                            <h2 className="text-xl font-semibold text-gray-900">攻击统计</h2>
                        </div>
                        <p className="text-gray-600">查看恶意攻击统计数据，管理高频攻击IP</p>
                    </button>
                </div>
            </div>
        </div>
    );
}

export default SystemManagePage;
