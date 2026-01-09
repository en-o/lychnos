import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, Save} from 'lucide-react';
import type {UserProfile} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';
import {authApi} from '../api/auth';

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [profile, setProfile] = useState<UserProfile>({
    loginName: '',
    nickname: '',
    email: '',
    avatar: '',
    createTime: '',
  });

  const [formData, setFormData] = useState<UserProfile>(profile);

  useEffect(() => {
    // 从 localStorage 加载用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const userProfile: UserProfile = {
      loginName: userInfo.loginName || '',
      nickname: userInfo.nickname || '',
      email: userInfo.email || '',
      avatar: userInfo.avatar || '',
      createTime: userInfo.createTime || new Date().toISOString(),
    };
    setProfile(userProfile);
    setFormData(userProfile);
  }, []);

  const handleSave = () => {
    // 保存到 localStorage
    const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const updatedUserInfo = {
      ...currentUserInfo,
      ...formData,
    };
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo));
    setProfile(formData);
    setIsEditing(false);

    // 调用 API 保存
    authApi.fixUserInfo({
      loginName: formData.loginName,
      nickname: formData.nickname || '',
      email: formData.email,
    });
    toast.success('保存成功');
  };

  const handleCancel = () => {
    setFormData(profile);
    setIsEditing(false);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-4xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/')}
              className="p-1.5 hover:bg-gray-100 rounded-lg transition"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
            <Logo className="w-5 h-5" />
            <span className="font-semibold text-gray-800">个人资料</span>
          </div>

          {!isEditing ? (
            <button
              onClick={() => setIsEditing(true)}
              className="px-4 py-1.5 text-sm text-blue-600 hover:bg-blue-50 rounded-lg transition"
            >
              编辑
            </button>
          ) : (
            <div className="flex gap-2">
              <button
                onClick={handleCancel}
                className="px-4 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded-lg transition"
              >
                取消
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-1.5 text-sm text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition flex items-center gap-1"
              >
                <Save className="w-4 h-4" />
                保存
              </button>
            </div>
          )}
        </div>
      </nav>

      {/* 主内容 */}
      <main className="pt-14">
        <div className="max-w-2xl mx-auto px-4 py-8">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            {/* 头像 */}
            <div className="flex items-center gap-4 mb-6 pb-6 border-b border-gray-200">
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white text-2xl font-medium">
                {profile.nickname?.[0] || profile.loginName?.[0] || 'U'}
              </div>
              <div>
                <h2 className="text-xl font-semibold text-gray-900">
                  {profile.nickname || profile.loginName}
                </h2>
                <p className="text-sm text-gray-500">@{profile.loginName}</p>
              </div>
            </div>

            {/* 表单 */}
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  用户名
                </label>
                <input
                  type="text"
                  value={profile.loginName}
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
                />
                <p className="text-xs text-gray-500 mt-1">用户名不可修改</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  昵称
                </label>
                <input
                  type="text"
                  value={formData.nickname}
                  onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
                  disabled={!isEditing}
                  className={`w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    !isEditing ? 'bg-gray-50' : ''
                  }`}
                  placeholder="请输入昵称"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  邮箱
                </label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  disabled={!isEditing}
                  className={`w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    !isEditing ? 'bg-gray-50' : ''
                  }`}
                  placeholder="请输入邮箱"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  注册时间
                </label>
                <input
                  type="text"
                  value={
                    profile.createTime
                      ? new Date(profile.createTime).toLocaleDateString('zh-CN')
                      : '-'
                  }
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                />
              </div>
            </div>
          </div>

          {/* 其他设置 */}
          <div className="mt-6 bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">账户安全</h3>
            <div className="space-y-3">
              <button
                onClick={() => navigate('/settings/password')}
                className="w-full px-4 py-3 text-left border border-gray-200 rounded-lg hover:bg-gray-50 transition flex items-center justify-between"
              >
                <span className="text-gray-900">修改密码</span>
                <span className="text-gray-400">→</span>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ProfilePage;
