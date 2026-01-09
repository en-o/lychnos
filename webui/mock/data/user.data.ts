/**
 * Mock数据 - 用户数据
 */

// Mock用户数据
export const mockUsersData = {
  admin: {
    loginName: 'admin',
    password: 'admin',
    userId: '1',
    nickname: '管理员',
    email: 'admin@example.com',
  },
  user: {
    loginName: 'user',
    password: 'user123',
    userId: '2',
    nickname: '普通用户',
    email: 'user@example.com',
  },
};

// 验证用户登录
export function validateUser(loginName: string, password: string) {
  const user = mockUsersData[loginName as keyof typeof mockUsersData];
  if (user && user.password === password) {
    return {
      loginName: user.loginName,
      userId: user.userId,
      nickname: user.nickname,
    };
  }
  return null;
}
