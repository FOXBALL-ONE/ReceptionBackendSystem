/**
 * 全局登录校验中间件（在 nuxt.config 的 router.middleware 中注册）。
 *
 * 仅在客户端校验：页面数据均在 onMounted 客户端拉取，SSR 阶段不触碰受保护接口，
 * 因此把登录态判定放在客户端，避免 SSR 无会话上下文导致的误跳转。
 */
export default defineNuxtRouteMiddleware((to) => {
    if (import.meta.server) {
        return;
    }

    const auth = useAuthStore();

    // 登录页放行；已登录再访问登录页则跳回首页或原目标页
    if (to.path === "/login") {
        if (auth.isAuthenticated) {
            return navigateTo(resolveRedirect(to) || "/");
        }
        return;
    }

    if (!auth.isAuthenticated) {
        return navigateTo({
            path: "/login",
            query: to.fullPath && to.fullPath !== "/" ? {redirect: to.fullPath} : {},
        });
    }
});

/** 读取并校验 redirect 参数，避免开放重定向 */
function resolveRedirect(to: { query: { redirect?: unknown } }): string {
    const raw = to.query.redirect;
    if (typeof raw !== "string" || !raw.startsWith("/") || raw.startsWith("//") || raw === "/login") {
        return "";
    }
    return raw;
}
