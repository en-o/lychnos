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

  // 检查是否包含签名参数（未登录用户的情况）
  // 格式: l:1:/path?expires=xxx&signature=yyy
  let actualPosterUrl = posterUrl;
  let signatureParams = '';

  const questionMarkIndex = posterUrl.indexOf('?');
  if (questionMarkIndex !== -1) {
    actualPosterUrl = posterUrl.substring(0, questionMarkIndex);
    signatureParams = posterUrl.substring(questionMarkIndex + 1);
  }

  // 解析格式: 协议:鉴权:路径
  // 使用正则匹配前两个字段，避免路径中的冒号被分割
  const match = actualPosterUrl.match(/^([^:]+):([^:]+):(.+)$/);

  if (!match) {
    // 无法解析，可能是旧的相对路径格式，通过后端代理
    // 确保路径拼接正确，避免出现 //image 的情况
    const baseUrl = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;
    let imageUrl = `${baseUrl}/image?path=${encodeURIComponent(actualPosterUrl)}`;

    // 添加签名参数（如果有）
    if (signatureParams) {
      imageUrl += `&${signatureParams}`;
    }

    return buildImageUrlWithToken(imageUrl);
  }

  const [, , auth, path] = match;

  // 无鉴权（0）- 直接访问完整 URL
  if (auth === '0') {
    return path;
  }

  // 有鉴权（1）- 通过后端代理访问，并附加 token 或签名
  // 确保路径拼接正确，避免出现 //image 的情况
  const baseUrl = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;
  let imageUrl = `${baseUrl}/image?path=${encodeURIComponent(actualPosterUrl)}`;

  // 添加签名参数（如果有，说明是未登录用户）
  if (signatureParams) {
    imageUrl += `&${signatureParams}`;
    return imageUrl; // 未登录用户使用签名，不需要 token
  }

  // 已登录用户添加 token
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
