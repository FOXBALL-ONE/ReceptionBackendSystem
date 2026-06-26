import {defineStore} from "pinia";

/** 登录用户信息，与后端 LoginUserView 对应 */
export interface LoginUser {
    id: number | null;
    username: string;
    displayName?: string | null;
    loginAt: number;
}

/** 登录第一步的响应（与后端 LoginResponse 对应） */
interface LoginResponse {
    twoFactorRequired: boolean;
    token?: string;
    user?: LoginUser;
    challengeToken?: string;
}

/**
 * 登录态 Store。
 *
 * 持有 JWT 令牌与用户信息，持久化到 localStorage；令牌由 useHttp 的 onRequest
 * 自动注入到每个请求的 Authorization 头，登录态因此跨浏览器/服务端重启持久。
 * challengeToken 是两步验证进行中的短时凭证，不持久化。
 */
export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: null as string | null,
        user: null as LoginUser | null,
        challengeToken: null as string | null,
    }),
    getters: {
        isAuthenticated: (state): boolean => !!state.token,
    },
    actions: {
        /**
         * 第一步登录。
         * - 未开启两步验证：保存令牌与用户。
         * - 已开启两步验证：保存 challengeToken，由调用方进入第二步。
         */
        async login(username: string, password: string): Promise<LoginResponse> {
            const {post} = useHttp();
            const result = await post<LoginResponse>(
                "/auth/login",
                {username, password},
                {payloadMode: "json"},
            );
            if (result.twoFactorRequired) {
                this.challengeToken = result.challengeToken ?? null;
            } else if (result.token) {
                this.token = result.token;
                this.user = result.user ?? null;
            }
            return result;
        },

        /** 第二步：用 TOTP 动态码完成登录。 */
        async verifyTotp(code: string) {
            const {post} = useHttp();
            const result = await post<LoginResponse>(
                "/auth/login/totp",
                {challengeToken: this.challengeToken, code},
                {payloadMode: "json"},
            );
            this.challengeToken = null;
            this.token = result.token ?? null;
            this.user = result.user ?? null;
        },

        /** 第二步：用备用码完成登录。 */
        async verifyBackup(backupCode: string) {
            const {post} = useHttp();
            const result = await post<LoginResponse>(
                "/auth/login/backup",
                {challengeToken: this.challengeToken, backupCode},
                {payloadMode: "json"},
            );
            this.challengeToken = null;
            this.token = result.token ?? null;
            this.user = result.user ?? null;
        },

        /** 注销：通知后端撤销令牌（请求会自动带上当前令牌），随后清空本地态 */
        async logout() {
            const {post} = useHttp();
            try {
                await post("/auth/logout", {}, {payloadMode: "json"});
            } finally {
                this.token = null;
                this.user = null;
                this.challengeToken = null;
            }
        },

        /** 查询当前登录用户，并据结果校正令牌有效性 */
        async fetchCurrent() {
            const {get} = useHttp();
            try {
                const user = await get<LoginUser | null>("/auth/current");
                if (user) {
                    this.user = user;
                } else {
                    this.token = null;
                    this.user = null;
                }
            } catch {
                this.token = null;
                this.user = null;
            }
        },

        /** 修改用户名：服务端撤销旧令牌并签发新令牌，这里替换本地令牌与用户信息 */
        async changeUsername(newUsername: string, password: string) {
            const {post} = useHttp();
            const result = await post<{token: string; user: LoginUser}>(
                "/account/username",
                {newUsername, password},
                {payloadMode: "json"},
            );
            this.token = result.token;
            this.user = result.user;
        },

        /** 修改密码：当前会话令牌保持有效，无需更新本地态 */
        async changePassword(oldPassword: string, newPassword: string) {
            const {post} = useHttp();
            await post("/account/password", {oldPassword, newPassword}, {payloadMode: "json"});
        },

        /** 两步验证状态 */
        async totpStatus(): Promise<{ enabled: boolean; pending: boolean }> {
            const {get} = useHttp();
            return await get<{ enabled: boolean; pending: boolean }>("/account/totp/status");
        },

        /** 发起两步验证设置，返回明文密钥与 otpauth URI */
        async totpSetup(): Promise<{ secret: string; otpauthUri: string }> {
            const {post} = useHttp();
            return await post<{ secret: string; otpauthUri: string }>(
                "/account/totp/setup",
                {},
                {payloadMode: "json"},
            );
        },

        /** 确认启用两步验证，返回一次性备用码 */
        async totpEnable(code: string): Promise<string[]> {
            const {post} = useHttp();
            return await post<string[]>("/account/totp/enable", {code}, {payloadMode: "json"});
        },

        /** 关闭两步验证 */
        async totpDisable(password: string) {
            const {post} = useHttp();
            await post("/account/totp/disable", {password}, {payloadMode: "json"});
        },

        /** 直接清空本地登录态（供 useHttp 检测到 401 时调用） */
        reset() {
            this.token = null;
            this.user = null;
            this.challengeToken = null;
        },
    },
    persist: {
        // 只持久化令牌与用户，不持久化两步验证进行中的 challengeToken
        pick: ["token", "user"],
    },
});
