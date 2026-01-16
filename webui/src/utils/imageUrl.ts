/**
 * 图片 URL 工具函数
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * 解析 poster_url 并返回可访问的图片 URL
 *
 * 格式: 协议:鉴权:路径
 * - 协议: h(HTTP/HTTPS) / ali(阿里云OSS) / qiniu(七牛云) / s3(AWS S3) / f(FTP) / l(本地)
 * - 鉴权: 0(无需鉴权) / 1(需要鉴权)
 *
 * @param posterUrl poster_url 格式字符串，例如: h:0:https://example.com/image.png
 * @returns 可访问的完整图片 URL
 */
export function getImageUrl(posterUrl: string | undefined | null): string {
  if (!posterUrl) return '';

  // 解析格式: 协议:鉴权:路径
  const parts = posterUrl.split(':', 3);

  // 兼容旧格式（直接是完整 URL）
  if (parts.length < 3) {
    // 如果是旧的完整 URL，直接返回
    if (posterUrl.startsWith('http://') || posterUrl.startsWith('https://')) {
      return posterUrl;
    }
    // 如果是旧的相对路径，通过后端代理
    return `${API_BASE_URL}/image?path=${encodeURIComponent(posterUrl)}`;
  }

  const [, auth, path] = parts;

  // 无鉴权（0）- 直接访问完整 URL
  if (auth === '0') {
    return path;
  }

  // 有鉴权（1）- 通过后端代理访问
  return `${API_BASE_URL}/image?path=${encodeURIComponent(posterUrl)}`;
}
