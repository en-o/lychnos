import React, {useEffect, useState} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {ArrowLeft, Brain, Check, Copy, Image as ImageIcon, Plus, Repeat, Trash2} from 'lucide-react';
import type {AIModelConfig} from '../models';
import Logo from '../components/Logo';
import {toast} from '../components/ToastContainer';
import ConfirmDialog from '../components/ConfirmDialog';
import {aiModelApi} from '../api/aiModel';

type TabType = 'analysis' | 'image';

// AI 厂商默认 API URL 配置
// 注意：Spring AI 会自动在 baseUrl 后添加 /v1，所以这里的 URL 不应包含 /v1
const AI_FACTORY_DEFAULTS: Record<string, { apiUrl: string; description?: string }> = {
  // 文本分析模型
  openai: { apiUrl: 'https://api.openai.com', description: 'OpenAI 官方 API' },
  ollama: { apiUrl: 'http://localhost:11434', description: 'Ollama 本地服务' },
  deepseek: { apiUrl: 'https://api.deepseek.com', description: 'DeepSeek API' },
  azure: { apiUrl: 'https://your-resource.openai.azure.com', description: 'Azure OpenAI Service' },
  anthropic: { apiUrl: 'https://api.anthropic.com', description: 'Anthropic Claude API' },
  qwen: { apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode', description: '阿里云通义千问' },
  baidu: { apiUrl: 'https://aip.baidubce.com/rpc/2.0/ai_custom', description: '百度文心一言' },
  modelscope: { apiUrl: 'https://api-inference.modelscope.cn', description: '魔搭社区' },
  huggingface: { apiUrl: 'https://api-inference.huggingface.co', description: 'Hugging Face' },
  // 图片生成模型
  'stable-diffusion': { apiUrl: 'http://localhost:7860', description: 'Stable Diffusion WebUI' },
  midjourney: { apiUrl: 'https://api.midjourney.com', description: 'Midjourney API' },
  'dall-e': { apiUrl: 'https://api.openai.com', description: 'DALL-E by OpenAI' },
  'nano-banana-pro': { apiUrl: 'https://api.nano-banana.com', description: 'Nano Banana Pro' },
  'modelscope-image': { apiUrl: 'https://api-inference.modelscope.cn', description: '魔搭社区图片生成' },
  'huggingface-image': { apiUrl: 'https://api-inference.huggingface.co/models', description: 'Hugging Face 图片生成' },
};

const ModelSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = (searchParams.get('tab') || 'analysis') as TabType;

  const [models, setModels] = useState<AIModelConfig[]>([]);
  const [officialModels, setOfficialModels] = useState<Array<{type: string; model: string}>>([]);
  const [activeModelId, setActiveModelId] = useState<string>('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingModel, setEditingModel] = useState<AIModelConfig | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<{show: boolean; id: string} | null>(null);
  const [convertConfirm, setConvertConfirm] = useState<{show: boolean; model: AIModelConfig | null}>({show: false, model: null});

  const [formData, setFormData] = useState({
    name: '',
    factory: '',
    apiKey: '',
    apiUrl: '',
    model: '',
    type: activeTab === 'analysis' ? 'TEXT' : 'IMAGE',
  });

  // 加载模型数据
  useEffect(() => {
    loadModels();
    loadOfficialModels();
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

  const loadOfficialModels = async () => {
    try {
      const modelType = activeTab === 'analysis' ? 'TEXT' : 'IMAGE';
      const response = await aiModelApi.listOfficialModels(modelType);
      if (response.success) {
        setOfficialModels(response.data || []);
      }
    } catch (error) {
      console.error('加载官方模型列表失败:', error);
      // 不显示错误提示，因为官方模型是可选的
    }
  };

  const handleTabChange = (tab: TabType) => {
    setSearchParams({ tab });
    setShowAddForm(false);
    setEditingModel(null);
  };

  // 处理厂商选择变化，自动填充默认 API URL
  const handleFactoryChange = (factory: string) => {
    const defaultConfig = AI_FACTORY_DEFAULTS[factory];
    setFormData({
      ...formData,
      factory,
      // 自动填充默认 URL，用户可以手动修改
      apiUrl: defaultConfig?.apiUrl || '',
    });
  };

  // 保存模型
  const handleSaveModel = async () => {
    if (!formData.name || !formData.factory || !formData.apiUrl || !formData.model) {
      toast.warning('请填写必填项');
      return;
    }

    try {
      const modelData: AIModelConfig = {
        name: formData.name,
        factory: formData.factory,
        model: formData.model,
        apiKey: formData.apiKey,
        apiUrl: formData.apiUrl,
        enabled: false, // 新增/复制的模型默认不启用
        type: formData.type as 'TEXT' | 'IMAGE',
      };

      if (editingModel && editingModel.id) {
        await aiModelApi.update(editingModel.id, modelData);
        toast.success('更新成功');
      } else {
        await aiModelApi.add(modelData);
        toast.success('添加成功');
      }

      // 如果保存的模型类型与当前标签页不一致，切换到对应标签页
      const targetTab = formData.type === 'TEXT' ? 'analysis' : 'image';
      if (targetTab !== activeTab) {
        setSearchParams({ tab: targetTab });
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
      apiKey: '', // 编辑时清空 API Key，用户可选择重新输入或留空保持不变
      apiUrl: model.apiUrl,
      type: model.type,
    });
    setShowAddForm(true);
  };

  const handleCopyModel = (model: AIModelConfig) => {
    setEditingModel(null);
    setFormData({
      name: `${model.name} (副本)`,
      factory: model.factory,
      model: model.model,
      apiKey: '', // 复制时清空 API Key，用户需重新输入
      apiUrl: model.apiUrl,
      type: model.type,
    });
    setShowAddForm(true);
    toast.success('已复制模型配置，请重新输入 API Key 后保存');
  };

  const handleConvertModelType = (model: AIModelConfig) => {
    setConvertConfirm({ show: true, model });
  };

  const confirmConvert = async () => {
    const model = convertConfirm.model;
    if (!model || !model.id) return;

    const newType = model.type === 'TEXT' ? 'IMAGE' : 'TEXT';
    const targetTab = newType === 'TEXT' ? 'analysis' : 'image';

    try {
      const modelData: AIModelConfig = {
        ...model,
        type: newType,
        enabled: false, // 转换后强制设为禁用
      };

      await aiModelApi.update(model.id, modelData);
      toast.success(`已转换为${newType === 'TEXT' ? 'AI分析' : 'AI生图'}模型，当前状态为禁用`);

      // 切换到目标标签页
      setSearchParams({ tab: targetTab });
      await loadModels();
      setConvertConfirm({ show: false, model: null });
    } catch (error) {
      console.error('转换模型类型失败:', error);
      toast.error('转换失败，请重试');
    }
  };

  const resetForm = () => {
    setShowAddForm(false);
    setEditingModel(null);
    setFormData({
      name: '',
      factory: activeTab === 'analysis' ? 'openai' : 'modelscope-image',
      apiKey: '',
      apiUrl: '',
      model: '',
      type: activeTab === 'analysis' ? 'TEXT' : 'IMAGE',
    });
  };

  const openAddForm = () => {
    setEditingModel(null);
    setFormData({
      name: '',
      factory: activeTab === 'analysis' ? 'openai' : 'modelscope-image',
      apiKey: '',
      apiUrl: '',
      model: '',
      type: activeTab === 'analysis' ? 'TEXT' : 'IMAGE',
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

            {/* 隐私协议说明 */}
            <div className="px-6 py-4 bg-blue-50 border-b border-blue-100">
              <h3 className="text-sm font-medium text-blue-900 mb-2">隐私与数据共享说明</h3>
              <ul className="text-xs text-blue-800 space-y-1.5">
                <li className="flex items-start gap-2">
                  <span className="text-blue-600 mt-0.5">🔐</span>
                  <span><strong>API 密钥安全：</strong>您的 API Key 已加密存储，仅用于您自己的分析请求，绝不会被共享或用于其他目的。</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-600 mt-0.5">📊</span>
                  <span><strong>分析结果共享：</strong>书籍分析结果（如内容摘要、推荐理由等）将用于改进服务质量和推荐系统。</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-600 mt-0.5">🤝</span>
                  <span><strong>互惠共享机制：</strong>当您分析过的书籍被他人搜索时，可直接使用您的分析结果，节省 AI 调用成本；同样，您也可以使用其他用户已分析的书籍结果。</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-600 mt-0.5">🛡️</span>
                  <span><strong>隐私保护：</strong>我们不会关联您的个人信息（如用户名、邮箱）与分析结果，仅保留匿名的分析内容。</span>
                </li>
              </ul>
            </div>

            {/* 官方模型展示 */}
            {officialModels.length > 0 && (
              <div className="px-6 py-4 bg-green-50 border-b border-green-100">
                <h3 className="text-sm font-medium text-green-900 mb-3">官方提供的模型</h3>
                <div className="space-y-2">
                  {officialModels.map((officialModel, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-3 text-sm bg-white rounded-lg px-4 py-2 border border-green-200"
                    >
                      <span className="text-green-600 font-medium">📌</span>
                      <span className="text-gray-700">
                        <span className="font-medium">类型:</span> {officialModel.type === 'TEXT' ? 'AI分析' : 'AI生图'}
                      </span>
                      <span className="text-gray-300">|</span>
                      <span className="text-gray-700">
                        <span className="font-medium">模型:</span> {officialModel.model}
                      </span>
                    </div>
                  ))}
                </div>
                <p className="text-xs text-green-700 mt-3">
                  💡 提示：当您未配置自己的模型时，系统将自动使用上述官方模型作为回退方案。
                </p>
              </div>
            )}

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
                          {/* API Key 显示 */}
                          <div className="mt-1">
                            {model.apiKey && model.apiKey.trim() !== '' ? (
                              <span className="text-xs text-green-600">🔒 {model.apiKey}</span>
                            ) : (
                              <span className="text-xs text-gray-400">密钥为空（当前连接可能不需要，如 Ollama）</span>
                            )}
                          </div>
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
                            onClick={() => handleCopyModel(model)}
                            className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition"
                            title="复制配置"
                          >
                            <Copy className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => handleConvertModelType(model)}
                            className="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition"
                            title={`转换为${model.type === 'TEXT' ? '生图' : '分析'}模型`}
                          >
                            <Repeat className="w-4 h-4" />
                          </button>

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
              {editingModel ? '编辑模型' : `添加${activeTab === 'analysis' ? 'AI分析' : 'AI生图'}模型`}
            </h3>

            {/* 推荐模型提示 */}
            {!editingModel && (
              <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <div className="flex items-start gap-2">
                  <svg className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div className="text-sm text-blue-800 flex-1">
                    <p className="font-medium mb-1">推荐模型配置</p>
                    {activeTab === 'analysis' ? (
                      <div className="text-xs">
                        <p className="mb-1">• <span className="font-medium">DeepSeek</span> - 性价比高，适合大量文本分析任务</p>
                        <a
                          href="https://platform.deepseek.com/"
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-700 underline"
                        >
                          前往 DeepSeek 官网
                          <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                          </svg>
                        </a>
                      </div>
                    ) : (
                      <div className="text-xs">
                        <p className="mb-1">• <span className="font-medium">魔搭社区 Z-Image-Turbo</span> - 高质量图片生成（异步调用）</p>
                        <p className="mb-1 text-gray-600">模型名称: Tongyi-MAI/Z-Image-Turbo</p>
                        <a
                          href="https://www.modelscope.cn/models/Tongyi-MAI/Z-Image-Turbo"
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-700 underline"
                        >
                          查看模型详情
                          <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                          </svg>
                        </a>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

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
                  onChange={(e) => handleFactoryChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {activeTab === 'analysis' ? (
                    <>
                      <option value="">请选择</option>
                      <option value="openai">OpenAI</option>
                      <option value="ollama">Ollama</option>
                      <option value="deepseek">DeepSeek</option>
                      <option value="azure">Azure OpenAI</option>
                      <option value="anthropic">Anthropic (Claude)</option>
                      <option value="qwen">通义千问 (阿里云)</option>
                      <option value="baidu">百度文心一言</option>
                      <option value="modelscope">魔搭社区 (ModelScope)</option>
                      <option value="huggingface">Hugging Face</option>
                      <option value="custom">自定义</option>
                    </>
                  ) : (
                    <>
                      <option value="">请选择</option>
                      <option value="modelscope-image">魔搭社区 (ModelScope)</option>
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
                  placeholder={editingModel ? "留空保持原有密钥不变" : "sk-... (部分厂家如 Ollama 可不填)"}
                />
                {editingModel && (
                  <p className="mt-1 text-xs text-gray-500">
                    <span className="text-green-600">🔒 已加密存储</span> - 如需更新密钥请重新输入，留空则保持不变
                  </p>
                )}
                {!editingModel && (
                  <p className="mt-1 text-xs text-gray-500">
                    密钥将自动加密存储，保障安全
                  </p>
                )}
                <div className="mt-2 p-2.5 bg-amber-50 border border-amber-200 rounded-lg">
                  <p className="text-sm text-amber-800 flex items-start gap-1.5">
                    <span className="text-base">⚠️</span>
                    <span>注意：请留意 API Key 的过期时间。本系统不会管理密钥过期，过期后仅通过异常提示，无专门的过期提醒功能。</span>
                  </p>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1">
                  API URL *
                  <span className="group relative inline-flex">
                    <svg className="w-4 h-4 text-gray-400 hover:text-gray-600 cursor-help" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className="invisible group-hover:visible absolute left-5 top-0 w-64 bg-gray-800 text-white text-xs rounded-lg p-2 shadow-lg z-10 whitespace-normal">
                      注意：Spring AI 会自动在 URL 后添加 /v1，因此使用 OpenAI 兼容接口时无需在 URL 末尾包含 /v1。
                      <br />例如：阿里云通义千问应填写 https://dashscope.aliyuncs.com/compatible-mode
                    </span>
                  </span>
                </label>
                <input
                  type="text"
                  value={formData.apiUrl}
                  onChange={(e) => setFormData({ ...formData, apiUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder={activeTab === 'analysis' ? 'https://api.openai.com' : 'https://api.stability.ai'}
                />
                <p className="mt-1 text-xs text-gray-500">
                  已自动填充厂商默认 URL，可根据需要修改
                </p>
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
      <ConfirmDialog
        isOpen={deleteConfirm?.show || false}
        title="删除模型配置"
        message="确定要删除这个模型配置吗？"
        type="danger"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteConfirm(null)}
      />

      {/* 转换类型确认对话框 */}
      <ConfirmDialog
        isOpen={convertConfirm?.show || false}
        title="转换模型类型"
        message={convertConfirm?.model ? `确定要将 "${convertConfirm.model.name}" 从 ${convertConfirm.model.type === 'TEXT' ? 'AI分析模型' : 'AI生图模型'} 转换为 ${convertConfirm.model.type === 'TEXT' ? 'AI生图模型' : 'AI分析模型'} 吗？\n\n注意：转换后模型将被禁用，需要重新设置为当前模型才能使用。` : ''}
        type="warning"
        onConfirm={confirmConvert}
        onCancel={() => setConvertConfirm({ show: false, model: null })}
      />
    </div>
  );
};

export default ModelSettingsPage;
