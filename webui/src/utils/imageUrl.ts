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

  // 兼容旧格式（直接是完整 URL）
  if (posterUrl.startsWith('http://') || posterUrl.startsWith('https://')) {
    return posterUrl;
  }

  // 解析格式: 协议:鉴权:路径
  // 使用正则匹配前两个字段，避免路径中的冒号被分割
  const match = posterUrl.match(/^([^:]+):([^:]+):(.+)$/);

  if (!match) {
    // 无法解析，可能是旧的相对路径格式，通过后端代理
    return buildImageUrlWithToken(`${API_BASE_URL}/image?path=${encodeURIComponent(posterUrl)}`);
  }

  const [, protocol, auth, path] = match;

  // 无鉴权（0）- 直接访问完整 URL
  if (auth === '0') {
    return path;
  }

  // 有鉴权（1）- 通过后端代理访问，并附加 token
  const imageUrl = `${API_BASE_URL}/image?path=${encodeURIComponent(posterUrl)}`;
  return buildImageUrlWithToken(imageUrl);
}

/**
 * 为图片 URL 添加 token 参数
 * @param url 基础 URL
 * @returns 带 token 的 URL
 */
function buildImageUrlWithToken(url: string): string {
  const token = localStorage.getItem('token');
  if (!token) {
    return url;
  }

  // 判断 URL 是否已有参数
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}token=${encodeURIComponent(token)}`;
}
