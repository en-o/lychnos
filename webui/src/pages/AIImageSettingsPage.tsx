import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, BookOpen, Check, Plus, Trash2} from 'lucide-react';
import type {AIModelConfig} from '../models';
import {aiModelApi} from '../api/aiModel';

const AIImageSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const [models, setModels] = useState<AIModelConfig[]>([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingModel, setEditingModel] = useState<AIModelConfig | null>(null);
  const [activeModelId, setActiveModelId] = useState<string>('');

  const [formData, setFormData] = useState({
    name: '',
    factory: 'stable-diffusion',
    apiKey: '',
    apiUrl: '',
  });

  useEffect(() => {
    loadModels();
  }, []);

  const loadModels = async () => {
    try {
      const response = await aiModelApi.list('IMAGE');
      if (response.success) {
        const modelList = response.data || [];
        setModels(modelList);
        const activeModel = modelList.find((m: AIModelConfig) => m.enabled);
        if (activeModel && activeModel.id) {
          setActiveModelId(activeModel.id);
        }
      }
    } catch (error) {
      console.error('加载模型列表失败:', error);
    }
  };

  const handleSaveModel = async () => {
    if (!formData.name || !formData.factory || !formData.apiUrl) {
      alert('请填写必填项');
      return;
    }

    try {
      const modelData: AIModelConfig = {
        name: formData.name,
        factory: formData.factory,
        model: formData.factory, // 图片模型使用 factory 作为 model
        apiKey: formData.apiKey,
        apiUrl: formData.apiUrl,
        enabled: false,
        type: 'IMAGE',
      };

      if (editingModel && editingModel.id) {
        await aiModelApi.update(editingModel.id, modelData);
      } else {
        await aiModelApi.add(modelData);
      }

      await loadModels();
      resetForm();
    } catch (error) {
      console.error('保存模型失败:', error);
      alert('保存失败，请重试');
    }
  };

  const handleDeleteModel = async (id: string) => {
    if (!id) return;
    if (window.confirm('确定要删除这个模型配置吗？')) {
      try {
        await aiModelApi.delete(id);
        await loadModels();
      } catch (error) {
        console.error('删除模型失败:', error);
        alert('删除失败，请重试');
      }
    }
  };

  const handleSetActive = async (id: string) => {
    if (!id) return;
    try {
      await aiModelApi.setActive(id);
      await loadModels();
    } catch (error) {
      console.error('设置激活模型失败:', error);
      alert('设置失败，请重试');
    }
  };

  const handleEditModel = (model: AIModelConfig) => {
    setEditingModel(model);
    setFormData({
      name: model.name,
      factory: model.factory,
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
      factory: 'stable-diffusion',
      apiKey: '',
      apiUrl: '',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
        <div className="max-w-4xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/')}
              className="p-1.5 hover:bg-gray-100 rounded-lg transition"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
            <BookOpen className="w-5 h-5 text-gray-800" />
            <span className="font-semibold text-gray-800">AI生图模型设置</span>
          </div>
        </div>
      </nav>

      <main className="pt-14">
        <div className="max-w-4xl mx-auto px-4 py-8">
          <div className="mb-6 flex justify-between items-center">
            <h2 className="text-2xl font-semibold text-gray-900">AI生图模型</h2>
            <button
              onClick={() => {
                setShowAddForm(true);
                setEditingModel(null);
                setFormData({
                  name: '',
                  factory: 'stable-diffusion',
                  apiKey: '',
                  apiUrl: '',
                });
              }}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              添加模型
            </button>
          </div>

          <div className="space-y-3">
            {models.map((model) => (
              <div
                key={model.id}
                className={`bg-white border rounded-lg p-4 ${
                  model.id === activeModelId ? 'border-blue-500 ring-2 ring-blue-100' : 'border-gray-200'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-medium text-gray-900">{model.name}</h3>
                      {model.id === activeModelId && (
                        <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full flex items-center gap-1">
                          <Check className="w-3 h-3" />
                          当前使用
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-gray-600 mb-2">
                      类型: {model.factory.toUpperCase()}
                    </p>
                    {model.apiUrl && (
                      <p className="text-xs text-gray-500">API: {model.apiUrl}</p>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    {model.id !== activeModelId && (
                      <button
                        onClick={() => model.id && handleSetActive(model.id)}
                        className="px-3 py-1 bg-blue-50 text-blue-600 rounded text-sm font-medium hover:bg-blue-100 transition"
                      >
                        设为当前
                      </button>
                    )}

                    <button
                      onClick={() => handleEditModel(model)}
                      className="p-2 text-gray-600 hover:bg-gray-100 rounded transition"
                    >
                      编辑
                    </button>

                    <button
                      onClick={() => model.id && handleDeleteModel(model.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded transition"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {showAddForm && (
            <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
              <div className="bg-white rounded-xl max-w-md w-full p-6">
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
                      placeholder="例如: Stable Diffusion XL"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      模型类型 *
                    </label>
                    <select
                      value={formData.factory}
                      onChange={(e) =>
                        setFormData({ ...formData, factory: e.target.value })
                      }
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="stable-diffusion">Stable Diffusion</option>
                      <option value="midjourney">Midjourney</option>
                      <option value="dall-e">DALL-E</option>
                      <option value="dall-e">Nano Banana Pro</option>
                      <option value="custom">自定义</option>
                    </select>
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
                      placeholder="https://api.stability.ai"
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
        </div>
      </main>
    </div>
  );
};

export default AIImageSettingsPage;
