/**
 * 客户端启动时与服务端对齐登录态。
 *
 * 本地 store 虽已持久化，但仍可能在浏览器保留有效会话 cookie（如重开浏览器）而 store 为空，
 * 或本地态过期而 cookie 失效。此处统一以 /auth/current 为准刷新一次。
 */
export default defineNuxtPlugin(async () => {
    const auth = useAuthStore();
    await auth.fetchCurrent();
});
