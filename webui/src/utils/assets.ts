/**
 * 获取静态资源的完整路径
 * 自动处理 base path 前缀
 *
 * @param path - 资源路径，例如 'site_icon.svg' 或 '/site_icon.svg'
 * @returns 完整的资源路径
 *
 * @example
 * getAssetUrl('site_icon.svg') // => '/lychnos/site_icon.svg' (when BASE_URL is '/lychnos/')
 * getAssetUrl('/site_icon.svg') // => '/lychnos/site_icon.svg'
 */
export function getAssetUrl(path: string): string {
  const baseUrl = import.meta.env.BASE_URL || '/';
  const cleanPath = path.startsWith('/') ? path.slice(1) : path;

  // 确保 baseUrl 以斜杠结尾
  const normalizedBase = baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`;

  return `${normalizedBase}${cleanPath}`;
}
