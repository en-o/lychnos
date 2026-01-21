/**
 * OAuth2 提供者信息
 */
export interface OAuth2Provider {
    type: string;
    name: string;
    iconUrl?: string;
    sortOrder: number;
}

/**
 * 用户第三方账户绑定信息
 */
export interface UserThirdPartyBinding {
    providerType: string;
    nickname?: string;
    avatarUrl?: string;
    email?: string;
    createTime: string;
}
