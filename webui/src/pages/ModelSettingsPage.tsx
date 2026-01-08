import React, {useEffect, useState} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {ArrowLeft, Brain, Check, Image as ImageIcon, Plus, Trash2} from 'lucide-react';
import type {AIAnalysisModel, AIImageModel} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';
import ConfirmDialog from '../components/ConfirmDialog';

type TabType = 'analysis' | 'image';

const ModelSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = (searchParams.get('tab') || 'analysis') as TabType;

  const [analysisModels, setAnalysisModels] = useState<AIAnalysisModel[]>([]);
  const [imageModels, setImageModels] = useState<AIImageModel[]>([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingModel, setEditingModel] = useState<AIAnalysisModel | AIImageModel | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<{show: boolean; id: string; type: TabType} | null>(null);

  const [analysisFormData, setAnalysisFormData] = useState({
    name: '',
    type: 'openai' as AIAnalysisModel['type'],
    apiKey: '',
    apiUrl: '',
    model: '',
  });

  const [imageFormData, setImageFormData] = useState({
    name: '',
    type: 'stable-diffusion' as AIImageModel['type'],
    apiKey: '',
    apiUrl: '',
  });

  // 加载模型数据
  useEffect(() => {
    const savedAnalysisModels = localStorage.getItem('aiAnalysisModels');
    if (savedAnalysisModels) {
      setAnalysisModels(JSON.parse(savedAnalysisModels));
    } else {
      const defaultModels: AIAnalysisModel[] = [
        {
          id: '1',
          name: 'OpenAI GPT-4',
          type: 'openai',
          model: 'gpt-4',
          enabled: true,
          isActive: true,
        },
      ];
      setAnalysisModels(defaultModels);
      localStorage.setItem('aiAnalysisModels', JSON.stringify(defaultModels));
    }

    const savedImageModels = localStorage.getItem('aiImageModels');
    if (savedImageModels) {
      setImageModels(JSON.parse(savedImageModels));
    } else {
      const defaultModels: AIImageModel[] = [
        {
          id: '1',
          name: 'Stable Diffusion',
          type: 'stable-diffusion',
          enabled: true,
          isActive: true,
        },
      ];
      setImageModels(defaultModels);
      localStorage.setItem('aiImageModels', JSON.stringify(defaultModels));
    }
  }, []);

  const handleTabChange = (tab: TabType) => {
    setSearchParams({ tab });
  };

  // 分析模型操作
  const saveAnalysisModels = (models: AIAnalysisModel[]) => {
    setAnalysisModels(models);
    localStorage.setItem('aiAnalysisModels', JSON.stringify(models));
  };

  const handleSaveAnalysisModel = () => {
    if (!analysisFormData.name || !analysisFormData.type) {
      toast.warning('请填写必填项');
      return;
    }

    if (editingModel && 'model' in editingModel) {
      const updated = analysisModels.map((m) =>
        m.id === editingModel.id ? { ...m, ...analysisFormData } : m
      );
      saveAnalysisModels(updated);
    } else {
      const newModel: AIAnalysisModel = {
        id: Date.now().toString(),
        ...analysisFormData,
        enabled: true,
        isActive: false,
      };
      saveAnalysisModels([...analysisModels, newModel]);
    }

    resetForm();
  };

  const handleDeleteAnalysisModel = (id: string) => {
    setDeleteConfirm({ show: true, id, type: 'analysis' });
  };

  const handleSetAnalysisActive = (id: string) => {
    const updated = analysisModels.map((m) => ({
      ...m,
      isActive: m.id === id,
    }));
    saveAnalysisModels(updated);
  };

  const handleToggleAnalysisEnabled = (id: string) => {
    const updated = analysisModels.map((m) =>
      m.id === id ? { ...m, enabled: !m.enabled } : m
    );
    saveAnalysisModels(updated);
  };

  // 生图模型操作
  const saveImageModels = (models: AIImageModel[]) => {
    setImageModels(models);
    localStorage.setItem('aiImageModels', JSON.stringify(models));
  };

  const handleSaveImageModel = () => {
    if (!imageFormData.name || !imageFormData.type) {
      toast.warning('请填写必填项');
      return;
    }

    if (editingModel && !('model' in editingModel)) {
      const updated = imageModels.map((m) =>
        m.id === editingModel.id ? { ...m, ...imageFormData } : m
      );
      saveImageModels(updated);
    } else {
      const newModel: AIImageModel = {
        id: Date.now().toString(),
        ...imageFormData,
        enabled: true,
        isActive: false,
      };
      saveImageModels([...imageModels, newModel]);
    }

    resetForm();
  };

  const handleDeleteImageModel = (id: string) => {
    setDeleteConfirm({ show: true, id, type: 'image' });
  };

  const handleSetImageActive = (id: string) => {
    const updated = imageModels.map((m) => ({
      ...m,
      isActive: m.id === id,
    }));
    saveImageModels(updated);
  };

  const handleToggleImageEnabled = (id: string) => {
    const updated = imageModels.map((m) =>
      m.id === id ? { ...m, enabled: !m.enabled } : m
    );
    saveImageModels(updated);
  };

  const handleEditModel = (model: AIAnalysisModel | AIImageModel, type: TabType) => {
    setEditingModel(model);
    if (type === 'analysis' && 'model' in model) {
      setAnalysisFormData({
        name: model.name,
        type: model.type as AIAnalysisModel['type'],
        apiKey: model.apiKey || '',
        apiUrl: model.apiUrl || '',
        model: model.model || '',
      });
    } else {
      setImageFormData({
        name: model.name,
        type: model.type as AIImageModel['type'],
        apiKey: model.apiKey || '',
        apiUrl: model.apiUrl || '',
      });
    }
    setShowAddForm(true);
  };

  const resetForm = () => {
    setShowAddForm(false);
    setEditingModel(null);
    setAnalysisFormData({
      name: '',
      type: 'openai',
      apiKey: '',
      apiUrl: '',
      model: '',
    });
    setImageFormData({
      name: '',
      type: 'stable-diffusion',
      apiKey: '',
      apiUrl: '',
    });
  };

  const currentModels = activeTab === 'analysis' ? analysisModels : imageModels;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航栏 */}
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
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
        <div className="max-w-6xl mx-auto flex">
          {/* 左侧菜单 */}
          <aside className="w-64 border-r border-gray-200 bg-white min-h-[calc(100vh-3.5rem)] p-4">
            <nav className="space-y-1">
              <button
                onClick={() => handleTabChange('analysis')}
                className={`w-full px-4 py-2.5 rounded-lg text-left flex items-center gap-3 transition ${
                  activeTab === 'analysis'
                    ? 'bg-gray-100 text-gray-900 font-medium'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                <Brain className="w-5 h-5" />
                AI分析模型
              </button>
              <button
                onClick={() => handleTabChange('image')}
                className={`w-full px-4 py-2.5 rounded-lg text-left flex items-center gap-3 transition ${
                  activeTab === 'image'
                    ? 'bg-gray-100 text-gray-900 font-medium'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                <ImageIcon className="w-5 h-5" />
                AI生图模型
              </button>
            </nav>
          </aside>

          {/* 右侧内容 */}
          <div className="flex-1 p-8">
            {/* 标题和添加按钮 */}
            <div className="mb-6 flex justify-between items-center">
              <h2 className="text-2xl font-semibold text-gray-900">
                {activeTab === 'analysis' ? 'AI分析模型' : 'AI生图模型'}
              </h2>
              <button
                onClick={() => {
                  setShowAddForm(true);
                  setEditingModel(null);
                }}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
              >
                <Plus className="w-4 h-4" />
                添加模型
              </button>
            </div>

            {/* 模型列表 */}
            <div className="space-y-3">
              {currentModels.map((model) => (
                <div
                  key={model.id}
                  className={`bg-white border rounded-lg p-4 ${
                    model.isActive ? 'border-blue-500 ring-2 ring-blue-100' : 'border-gray-200'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-medium text-gray-900">{model.name}</h3>
                        {model.isActive && (
                          <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full flex items-center gap-1">
                            <Check className="w-3 h-3" />
                            当前使用
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-gray-600 mb-2">
                        类型: {model.type.toUpperCase()}
                        {'model' in model && model.model && ` | 模型: ${model.model}`}
                      </p>
                      {model.apiUrl && (
                        <p className="text-xs text-gray-500">API: {model.apiUrl}</p>
                      )}
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() =>
                          activeTab === 'analysis'
                            ? handleToggleAnalysisEnabled(model.id)
                            : handleToggleImageEnabled(model.id)
                        }
                        className={`px-3 py-1 rounded text-sm font-medium transition ${
                          model.enabled
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-100 text-gray-600'
                        }`}
                      >
                        {model.enabled ? '已启用' : '已禁用'}
                      </button>

                      {!model.isActive && model.enabled && (
                        <button
                          onClick={() =>
                            activeTab === 'analysis'
                              ? handleSetAnalysisActive(model.id)
                              : handleSetImageActive(model.id)
                          }
                          className="px-3 py-1 bg-blue-50 text-blue-600 rounded text-sm font-medium hover:bg-blue-100 transition"
                        >
                          设为当前
                        </button>
                      )}

                      <button
                        onClick={() => handleEditModel(model, activeTab)}
                        className="p-2 text-gray-600 hover:bg-gray-100 rounded transition"
                      >
                        编辑
                      </button>

                      <button
                        onClick={() =>
                          activeTab === 'analysis'
                            ? handleDeleteAnalysisModel(model.id)
                            : handleDeleteImageModel(model.id)
                        }
                        className="p-2 text-red-600 hover:bg-red-50 rounded transition"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
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

            {activeTab === 'analysis' ? (
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    模型名称 *
                  </label>
                  <input
                    type="text"
                    value={analysisFormData.name}
                    onChange={(e) =>
                      setAnalysisFormData({ ...analysisFormData, name: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="例如: OpenAI GPT-4"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    模型类型 *
                  </label>
                  <select
                    value={analysisFormData.type}
                    onChange={(e) =>
                      setAnalysisFormData({
                        ...analysisFormData,
                        type: e.target.value as AIAnalysisModel['type'],
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="openai">OpenAI</option>
                    <option value="ollama">Ollama</option>
                    <option value="deepseek">DeepSeek</option>
                    <option value="custom">自定义</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    模型名称
                  </label>
                  <input
                    type="text"
                    value={analysisFormData.model}
                    onChange={(e) =>
                      setAnalysisFormData({ ...analysisFormData, model: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="例如: gpt-4, llama2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    API Key
                  </label>
                  <input
                    type="password"
                    value={analysisFormData.apiKey}
                    onChange={(e) =>
                      setAnalysisFormData({ ...analysisFormData, apiKey: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="sk-..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    API URL
                  </label>
                  <input
                    type="text"
                    value={analysisFormData.apiUrl}
                    onChange={(e) =>
                      setAnalysisFormData({ ...analysisFormData, apiUrl: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="https://api.openai.com/v1"
                  />
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    模型名称 *
                  </label>
                  <input
                    type="text"
                    value={imageFormData.name}
                    onChange={(e) =>
                      setImageFormData({ ...imageFormData, name: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="例如: Stable Diffusion XL"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    模型类型 *
                  </label>
                  <select
                    value={imageFormData.type}
                    onChange={(e) =>
                      setImageFormData({
                        ...imageFormData,
                        type: e.target.value as AIImageModel['type'],
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="stable-diffusion">Stable Diffusion</option>
                    <option value="midjourney">Midjourney</option>
                    <option value="dall-e">DALL-E</option>
                    <option value="custom">自定义</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    API Key
                  </label>
                  <input
                    type="password"
                    value={imageFormData.apiKey}
                    onChange={(e) =>
                      setImageFormData({ ...imageFormData, apiKey: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    API URL
                  </label>
                  <input
                    type="text"
                    value={imageFormData.apiUrl}
                    onChange={(e) =>
                      setImageFormData({ ...imageFormData, apiUrl: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="https://api.stability.ai/v1"
                  />
                </div>
              </div>
            )}

            <div className="flex gap-3 mt-6">
              <button
                onClick={resetForm}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
              >
                取消
              </button>
              <button
                onClick={
                  activeTab === 'analysis' ? handleSaveAnalysisModel : handleSaveImageModel
                }
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
          onConfirm={() => {
            if (deleteConfirm.type === 'analysis') {
              saveAnalysisModels(analysisModels.filter((m) => m.id !== deleteConfirm.id));
            } else {
              saveImageModels(imageModels.filter((m) => m.id !== deleteConfirm.id));
            }
            setDeleteConfirm(null);
          }}
          onCancel={() => setDeleteConfirm(null)}
        />
      )}
    </div>
  );
};

export default ModelSettingsPage;
