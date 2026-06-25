import {defineStore} from "pinia";

/** 登录用户信息，与后端 LoginUserView 对应 */
export interface LoginUser {
    id: number | null;
    username: string;
    displayName?: string | null;
    loginAt: number;
}

/** 登录响应，与后端 LoginResponse 对应 */
interface LoginResult {
    token: string;
    user: LoginUser;
}

/**
 * 登录态 Store。
 *
 * 持有 JWT 令牌与用户信息，并持久化到 localStorage；令牌由 useHttp 的 onRequest
 * 自动注入到每个请求的 Authorization 头，登录态因此跨浏览器/服务端重启持久。
 */
export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: null as string | null,
        user: null as LoginUser | null,
    }),
    getters: {
        isAuthenticated: (state): boolean => !!state.token,
    },
    actions: {
        /** 登录：成功后保存令牌与用户信息 */
        async login(username: string, password: string) {
            const {post} = useHttp();
            const result = await post<LoginResult>(
                "/auth/login",
                {username, password},
                {payloadMode: "json"},
            );
            this.token = result.token;
            this.user = result.user;
        },

        /** 注销：通知后端撤销令牌（请求会自动带上当前令牌），随后清空本地态 */
        async logout() {
            const {post} = useHttp();
            try {
                await post("/auth/logout", {}, {payloadMode: "json"});
            } finally {
                this.token = null;
                this.user = null;
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
            const result = await post<{ token: string; user: LoginUser }>(
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

        /** 直接清空本地登录态（供 useHttp 检测到 401 时调用） */
        reset() {
            this.token = null;
            this.user = null;
        },
    },
    persist: true,
});
