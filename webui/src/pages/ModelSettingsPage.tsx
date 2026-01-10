import React, {useEffect, useState} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {ArrowLeft, Brain, Check, Image as ImageIcon, Plus, Trash2} from 'lucide-react';
import type {AIModelConfig} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';
import ConfirmDialog from '../components/ConfirmDialog';
import {aiModelApi} from '../api/aiModel';

type TabType = 'analysis' | 'image';

const ModelSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = (searchParams.get('tab') || 'analysis') as TabType;

  const [models, setModels] = useState<AIModelConfig[]>([]);
  const [activeModelId, setActiveModelId] = useState<string>('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingModel, setEditingModel] = useState<AIModelConfig | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<{show: boolean; id: string} | null>(null);

  const [formData, setFormData] = useState({
    name: '',
    factory: '',
    apiKey: '',
    apiUrl: '',
    model: '',
  });

  // 加载模型数据
  useEffect(() => {
    loadModels();
  }, [activeTab]);

  const loadModels = async () => {
    try {
      const modelType = activeTab === 'analysis' ? 'TEXT' : 'IMAGE';
      const response = await aiModelApi.list(modelType);
      if (response.success) {
        const modelList = response.data || [];
        setModels(modelList);
        const activeModel = modelList.find((m) => m.enabled);
        if (activeModel && activeModel.id) {
          setActiveModelId(activeModel.id);
        } else {
          setActiveModelId('');
        }
      }
    } catch (error) {
      console.error('加载模型列表失败:', error);
      toast.error('加载模型列表失败');
    }
  };

  const handleTabChange = (tab: TabType) => {
    setSearchParams({ tab });
    setShowAddForm(false);
    setEditingModel(null);
  };

  // 保存模型
  const handleSaveModel = async () => {
    if (!formData.name || !formData.factory || !formData.apiUrl || !formData.model) {
      toast.warning('请填写必填项');
      return;
    }

    try {
      const modelType = activeTab === 'analysis' ? 'TEXT' : 'IMAGE';
      const modelData: AIModelConfig = {
        name: formData.name,
        factory: formData.factory,
        model: formData.model,
        apiKey: formData.apiKey,
        apiUrl: formData.apiUrl,
        enabled: false,
        type: modelType,
      };

      if (editingModel && editingModel.id) {
        await aiModelApi.update(editingModel.id, modelData);
        toast.success('更新成功');
      } else {
        await aiModelApi.add(modelData);
        toast.success('添加成功');
      }

      await loadModels();
      resetForm();
    } catch (error) {
      console.error('保存模型失败:', error);
      toast.error('保存失败，请重试');
    }
  };

  const handleDeleteModel = (id: string) => {
    setDeleteConfirm({ show: true, id });
  };

  const confirmDelete = async () => {
    if (!deleteConfirm?.id) return;

    try {
      await aiModelApi.delete(deleteConfirm.id);
      toast.success('删除成功');
      await loadModels();
      setDeleteConfirm(null);
    } catch (error) {
      console.error('删除模型失败:', error);
      toast.error('删除失败，请重试');
    }
  };

  const handleSetActive = async (id: string) => {
    if (!id) return;
    try {
      await aiModelApi.setActive(id);
      toast.success('设置成功');
      await loadModels();
    } catch (error) {
      console.error('设置激活模型失败:', error);
      toast.error('设置失败，请重试');
    }
  };

  const handleEditModel = (model: AIModelConfig) => {
    setEditingModel(model);
    setFormData({
      name: model.name,
      factory: model.factory,
      model: model.model,
      apiKey: model.apiKey || '',
      apiUrl: model.apiUrl,
    });
    setShowAddForm(true);
  };

  const resetForm = () => {
    setShowAddForm(false);
    setEditingModel(null);
    setFormData({
      name: '',
      factory: activeTab === 'analysis' ? 'openai' : 'stable-diffusion',
      apiKey: '',
      apiUrl: '',
      model: '',
    });
  };

  const openAddForm = () => {
    setEditingModel(null);
    setFormData({
      name: '',
      factory: activeTab === 'analysis' ? 'openai' : 'stable-diffusion',
      apiKey: '',
      apiUrl: '',
      model: '',
    });
    setShowAddForm(true);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航栏 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-5xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/')}
              className="p-1.5 hover:bg-gray-100 rounded-lg transition"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
            <Logo className="w-5 h-5" />
            <span className="font-semibold text-gray-800">AI模型设置</span>
          </div>
        </div>
      </nav>

      {/* 主内容 */}
      <main className="pt-14">
        <div className="max-w-5xl mx-auto px-4 py-8">
          <div className="bg-white rounded-lg border border-gray-200">
            {/* Tab 切换和添加按钮 */}
            <div className="px-6 py-4 border-b border-gray-200">
              <div className="flex justify-between items-center">
                <div className="flex gap-1 p-1 bg-gray-100 rounded-lg">
                  <button
                    onClick={() => handleTabChange('analysis')}
                    className={`px-4 py-2 rounded-md text-sm font-medium transition flex items-center gap-2 ${
                      activeTab === 'analysis'
                        ? 'bg-white text-gray-900 shadow-sm'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    <Brain className="w-4 h-4" />
                    AI分析模型
                  </button>
                  <button
                    onClick={() => handleTabChange('image')}
                    className={`px-4 py-2 rounded-md text-sm font-medium transition flex items-center gap-2 ${
                      activeTab === 'image'
                        ? 'bg-white text-gray-900 shadow-sm'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    <ImageIcon className="w-4 h-4" />
                    AI生图模型
                  </button>
                </div>
                <button
                  onClick={openAddForm}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
                >
                  <Plus className="w-4 h-4" />
                  添加模型
                </button>
              </div>
            </div>

            {/* 模型列表 */}
            <div className="p-6">
              {models.length === 0 ? (
                <div className="text-center py-12">
                  <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
                    {activeTab === 'analysis' ? (
                      <Brain className="w-8 h-8 text-gray-400" />
                    ) : (
                      <ImageIcon className="w-8 h-8 text-gray-400" />
                    )}
                  </div>
                  <p className="text-gray-500 mb-4">暂无模型配置</p>
                  <button
                    onClick={openAddForm}
                    className="text-blue-600 hover:text-blue-700 font-medium"
                  >
                    立即添加
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {models.map((model) => (
                    <div
                      key={model.id}
                      className={`border rounded-lg p-4 transition ${
                        model.id === activeModelId
                          ? 'border-blue-500 bg-blue-50/50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-2">
                            <h3 className="font-medium text-gray-900 truncate">{model.name}</h3>
                            {model.id === activeModelId && (
                              <span className="px-2 py-0.5 bg-blue-600 text-white text-xs rounded-full flex items-center gap-1 flex-shrink-0">
                                <Check className="w-3 h-3" />
                                使用中
                              </span>
                            )}
                          </div>
                          <div className="flex items-center gap-3 text-sm text-gray-600 mb-1">
                            <span className="font-medium">{model.factory.toUpperCase()}</span>
                            {model.model && (
                              <>
                                <span className="text-gray-300">|</span>
                                <span>{model.model}</span>
                              </>
                            )}
                          </div>
                          {model.apiUrl && (
                            <p className="text-xs text-gray-500 truncate">{model.apiUrl}</p>
                          )}
                        </div>

                        <div className="flex items-center gap-2 ml-4 flex-shrink-0">
                          {model.id !== activeModelId && (
                            <button
                              onClick={() => model.id && handleSetActive(model.id)}
                              className="px-3 py-1.5 border border-blue-600 text-blue-600 rounded-lg text-sm font-medium hover:bg-blue-50 transition"
                            >
                              设为当前
                            </button>
                          )}

                          <button
                            onClick={() => handleEditModel(model)}
                            className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
                            title="编辑"
                          >
                            编辑
                          </button>

                          <button
                            onClick={() => model.id && handleDeleteModel(model.id)}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                            title="删除"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>

      {/* 添加/编辑表单弹窗 */}
      {showAddForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-semibold text-gray-900 mb-4">
              {editingModel ? '编辑模型' : '添加模型'}
            </h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  配置名称 *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="例如: 我的 GPT-4 配置"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  API 厂家 *
                </label>
                <select
                  value={formData.factory}
                  onChange={(e) => setFormData({ ...formData, factory: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {activeTab === 'analysis' ? (
                    <>
                      <option value="">请选择</option>
                      <option value="openai">OpenAI</option>
                      <option value="ollama">Ollama</option>
                      <option value="deepseek">DeepSeek</option>
                      <option value="azure">Azure</option>
                      <option value="anthropic">Anthropic</option>
                      <option value="qwen">通义千问</option>
                      <option value="baidu">百度</option>
                      <option value="custom">自定义</option>
                    </>
                  ) : (
                    <>
                      <option value="">请选择</option>
                      <option value="stable-diffusion">Stable Diffusion</option>
                      <option value="midjourney">Midjourney</option>
                      <option value="dall-e">DALL-E</option>
                      <option value="dall-e">Nano Banana Pro</option>
                      <option value="custom">自定义</option>
                    </>
                  )}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  模型名称 *
                </label>
                <input
                  type="text"
                  value={formData.model}
                  onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder={activeTab === 'analysis' ? '例如: gpt-4, deepseek-chat' : '例如: stable-diffusion-xl'}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  API Key
                </label>
                <input
                  type="password"
                  value={formData.apiKey}
                  onChange={(e) => setFormData({ ...formData, apiKey: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="sk-... (部分厂家如 Ollama 可不填)"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  API URL *
                </label>
                <input
                  type="text"
                  value={formData.apiUrl}
                  onChange={(e) => setFormData({ ...formData, apiUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder={activeTab === 'analysis' ? 'https://api.openai.com/v1' : 'https://api.stability.ai/v1'}
                />
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={resetForm}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
              >
                取消
              </button>
              <button
                onClick={handleSaveModel}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 删除确认对话框 */}
      {deleteConfirm?.show && (
        <ConfirmDialog
          message="确定要删除这个模型配置吗？"
          onConfirm={confirmDelete}
          onCancel={() => setDeleteConfirm(null)}
        />
      )}
    </div>
  );
};

export default ModelSettingsPage;
