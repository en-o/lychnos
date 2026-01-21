/**
 * 生成随机字符串
 * @param length 长度
 */
export const generateRandomString = (length: number = 10): string => {
    return Math.random().toString(36).substring(2, 2 + length);
};
