import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, BookOpen, Check, Plus, Trash2} from 'lucide-react';
import type {AIModelConfig} from '../models';
import {aiModelApi} from '../api/aiModel';

const AIAnalysisSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const [models, setModels] = useState<AIModelConfig[]>([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingModel, setEditingModel] = useState<AIModelConfig | null>(null);
  const [activeModelId, setActiveModelId] = useState<string>('');

  // 表单数据
  const [formData, setFormData] = useState({
    name: '',
    factory: 'openai',
    apiKey: '',
    apiUrl: '',
    model: '',
  });

  // 加载模型列表
  useEffect(() => {
    loadModels();
  }, []);

  const loadModels = async () => {
    try {
      const response = await aiModelApi.list('TEXT');
      if (response.success) {
        const modelList = response.data || [];
        setModels(modelList);
        // 找出启用的模型作为当前激活的
        const activeModel = modelList.find((m: AIModelConfig) => m.enabled);
        if (activeModel && activeModel.id) {
          setActiveModelId(activeModel.id);
        }
      }
    } catch (error) {
      console.error('加载模型列表失败:', error);
    }
  };

  // 添加/编辑模型
  const handleSaveModel = async () => {
    if (!formData.name || !formData.factory || !formData.model || !formData.apiUrl) {
      alert('请填写必填项');
      return;
    }

    try {
      const modelData: AIModelConfig = {
        name: formData.name,
        factory: formData.factory,
        apiKey: formData.apiKey,
        apiUrl: formData.apiUrl,
        model: formData.model,
        enabled: false,
        type: 'TEXT',
      };

      if (editingModel && editingModel.id) {
        // 编辑模式
        await aiModelApi.update(editingModel.id, modelData);
      } else {
        // 新增模式
        await aiModelApi.add(modelData);
      }

      // 重新加载列表
      await loadModels();

      // 重置表单
      resetForm();
    } catch (error) {
      console.error('保存模型失败:', error);
      alert('保存失败，请重试');
    }
  };

  // 删除模型
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

  // 设置为当前使用的模型
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

  // 编辑模型
  const handleEditModel = (model: AIModelConfig) => {
    setEditingModel(model);
    setFormData({
      name: model.name,
      factory: model.factory,
      apiKey: model.apiKey || '',
      apiUrl: model.apiUrl,
      model: model.model,
    });
    setShowAddForm(true);
  };

  const resetForm = () => {
    setShowAddForm(false);
    setEditingModel(null);
    setFormData({
      name: '',
      factory: 'openai',
      apiKey: '',
      apiUrl: '',
      model: '',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航栏 */}
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
            <span className="font-semibold text-gray-800">AI分析模型设置</span>
          </div>
        </div>
      </nav>

      {/* 主内容 */}
      <main className="pt-14">
        <div className="max-w-4xl mx-auto px-4 py-8">
          {/* 添加模型按钮 */}
          <div className="mb-6 flex justify-between items-center">
            <h2 className="text-2xl font-semibold text-gray-900">AI分析模型</h2>
            <button
              onClick={() => {
                setShowAddForm(true);
                setEditingModel(null);
                setFormData({
                  name: '',
                  factory: 'openai',
                  apiKey: '',
                  apiUrl: '',
                  model: '',
                });
              }}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              添加模型
            </button>
          </div>

          {/* 模型列表 */}
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
                      {model.model && ` | 模型: ${model.model}`}
                    </p>
                    {model.apiUrl && (
                      <p className="text-xs text-gray-500">API: {model.apiUrl}</p>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    {/* 设为当前 */}
                    {model.id !== activeModelId && (
                      <button
                        onClick={() => model.id && handleSetActive(model.id)}
                        className="px-3 py-1 bg-blue-50 text-blue-600 rounded text-sm font-medium hover:bg-blue-100 transition"
                      >
                        设为当前
                      </button>
                    )}

                    {/* 编辑 */}
                    <button
                      onClick={() => handleEditModel(model)}
                      className="p-2 text-gray-600 hover:bg-gray-100 rounded transition"
                    >
                      编辑
                    </button>

                    {/* 删除 */}
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

          {/* 添加/编辑表单 */}
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
                      placeholder="例如: 我的 GPT-4 配置"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      API 厂家 *
                    </label>
                    <select
                      value={formData.factory}
                      onChange={(e) =>
                        setFormData({ ...formData, factory: e.target.value })
                      }
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="openai">OpenAI</option>
                      <option value="ollama">Ollama</option>
                      <option value="deepseek">DeepSeek</option>
                      <option value="azure">Azure</option>
                      <option value="anthropic">Anthropic</option>
                      <option value="qwen">通义千问</option>
                      <option value="baidu">百度</option>
                      <option value="custom">自定义</option>
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
                      placeholder="例如: gpt-4, deepseek-chat"
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
                      placeholder="https://api.openai.com/v1"
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

export default AIAnalysisSettingsPage;
