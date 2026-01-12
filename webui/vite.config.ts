import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'
import {viteMockServe} from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig(({mode}) => {
  // 加载 .env 文件中的环境变量
  const env = loadEnv(mode, process.cwd(), '')

  // 优先使用命令行参数，其次使用 .env 文件，最后使用默认值
  const isMock = process.env.VITE_USE_MOCK === 'true' || env.VITE_USE_MOCK === 'true'
  const apiBaseUrl = process.env.VITE_API_BASE_URL || env.VITE_API_BASE_URL || '/api'
  const basePath = process.env.VITE_BASE_PATH || env.VITE_BASE_PATH || '/'

  console.log(`[Vite Config] Mock: ${isMock}, API Base: ${apiBaseUrl}, Base Path: ${basePath}`)

  return {
    base: basePath,
    plugins: [
      react(),
      viteMockServe({
        mockPath: 'mock', // mock文件目录
        enable: isMock, // 是否启用mock
        watchFiles: true, // 监听文件变化
      }),
    ],
    server: {
      port: 3000,
      // 真实接口模式下才配置代理
      // 代理路径必须与 request.ts 中的 baseURL 一致
      proxy: !isMock ? {
        [apiBaseUrl]: {  // 使用环境变量，确保与 request.ts 一致
          target: 'http://localhost:1250',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiBaseUrl}`), '')
        }
      } : undefined
    }
  }
})
