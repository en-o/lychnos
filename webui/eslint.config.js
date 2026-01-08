import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import {defineConfig, globalIgnores} from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      // 放宽 any 类型限制 - 在某些场景下（泛型默认值、第三方库类型不完整）any 是合理的
      '@typescript-eslint/no-explicit-any': 'warn', // 改为警告而不是错误

      // 或者完全禁用（如果你觉得警告也烦）
      // '@typescript-eslint/no-explicit-any': 'off',
    },
  },
])
