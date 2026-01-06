import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { viteMockServe } from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig(() => {
  const isMock = process.env.VITE_USE_MOCK === 'true'

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
      proxy: isMock ? undefined : {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})
