import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import {viteMockServe} from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig(() => {
  const isMock = process.env.VITE_USE_MOCK === 'true'
  const apiBaseUrl = process.env.VITE_API_BASE_URL || '/api'

  console.log(`[Vite Config] Mock: ${isMock}, API Base: ${apiBaseUrl}`)

  return {
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
          target: 'http://localhost:6125',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiBaseUrl}`), '')
        }
      } : undefined
    }
  }
})
