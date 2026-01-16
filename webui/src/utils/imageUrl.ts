/**
 * 图片 URL 工具函数
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * 将后端返回的相对路径转换为完整的图片访问URL
 * @param relativePath 相对路径，格式：yyyyMMdd/书名.png
 * @returns 完整的图片访问URL
 */
export function getImageUrl(relativePath: string | undefined | null): string {
  if (!relativePath) return '';

  // 如果已经是完整URL，直接返回
  if (relativePath.startsWith('http://') || relativePath.startsWith('https://')) {
    return relativePath;
  }

  // 构建完整URL：/api/image?path=yyyyMMdd/书名.png
  return `${API_BASE_URL}/image?path=${encodeURIComponent(relativePath)}`;
}
