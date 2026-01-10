/**
 * Mock数据 - AI模型数据
 */
import type {AIModelConfig} from '../../src/models';

// Mock AI模型数据
let mockAnalysisModels: AIModelConfig[] = [
  {
    id: '1',
    userId: '1',
    name: 'OpenAI GPT-4',
    model: 'gpt-4',
    factory: 'openai',
    apiKey: 'sk-mock-key-***',
    apiUrl: 'https://api.openai.com/v1',
    enabled: true,
    type: 'TEXT',
  },
  {
    id: '2',
    userId: '1',
    name: 'DeepSeek Chat',
    model: 'deepseek-chat',
    factory: 'deepseek',
    apiKey: 'sk-mock-key-***',
    apiUrl: 'https://api.deepseek.com/v1',
    enabled: false,
    type: 'TEXT',
  },
];

let mockImageModels: AIModelConfig[] = [
  {
    id: '3',
    userId: '1',
    name: 'Stable Diffusion XL',
    model: 'stable-diffusion',
    factory: 'stable-diffusion',
    apiKey: 'mock-key-***',
    apiUrl: 'https://api.stability.ai/v1',
    enabled: true,
    type: 'IMAGE',
  },
];

let nextId = 4;

export function getModelsByType(type: 'TEXT' | 'IMAGE'): AIModelConfig[] {
  return type === 'TEXT' ? [...mockAnalysisModels] : [...mockImageModels];
}

export function addModel(model: AIModelConfig): AIModelConfig {
  const newModel = {
    ...model,
    id: String(nextId++),
    userId: '1',
  };

  if (model.type === 'TEXT') {
    mockAnalysisModels.push(newModel);
  } else {
    mockImageModels.push(newModel);
  }

  return newModel;
}

export function updateModel(id: string, model: AIModelConfig): AIModelConfig | null {
  const models = model.type === 'TEXT' ? mockAnalysisModels : mockImageModels;
  const index = models.findIndex((m) => m.id === id);

  if (index !== -1) {
    models[index] = { ...models[index], ...model, id };
    return models[index];
  }

  return null;
}

export function deleteModel(id: string): boolean {
  let index = mockAnalysisModels.findIndex((m) => m.id === id);
  if (index !== -1) {
    mockAnalysisModels.splice(index, 1);
    return true;
  }

  index = mockImageModels.findIndex((m) => m.id === id);
  if (index !== -1) {
    mockImageModels.splice(index, 1);
    return true;
  }

  return false;
}

export function setActiveModel(id: string): boolean {
  // 先尝试在文本模型中查找
  let model = mockAnalysisModels.find((m) => m.id === id);
  if (model) {
    mockAnalysisModels.forEach((m) => (m.enabled = false));
    model.enabled = true;
    return true;
  }

  // 再尝试在图片模型中查找
  model = mockImageModels.find((m) => m.id === id);
  if (model) {
    mockImageModels.forEach((m) => (m.enabled = false));
    model.enabled = true;
    return true;
  }

  return false;
}
