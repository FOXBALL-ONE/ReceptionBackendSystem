import {createFetch, type FetchOptions} from "ofetch";
import type {ApiResult} from "~/types/http";

type ParamMode = "query" | "json";
type QueryParams = Record<string, unknown>;
type JsonBody = BodyInit | Record<string, any> | null | undefined;

export interface HttpRequestOptions<T> extends Omit<FetchOptions<"json">, "baseURL" | "query" | "params" | "body"> {
    method?: "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
    payloadMode?: ParamMode;
    params?: QueryParams;
    body?: T;
}

const SUCCESS_CODE = "200";
const SUCCESS_CODE_ALTERNATE = "0";

function getResponseCode(response: ApiResult<unknown>) {
    const rawCode = response.code ?? response.status;
    if (rawCode === null || rawCode === undefined) {
        return "";
    }

    return String(rawCode);
}

function getResponseMessage(response: ApiResult<unknown>) {
    return response.msg ?? response.message ?? "Request failed";
}

/**
 * 处理后端返回 401（未登录 / 会话过期）：清空本地登录态并跳转登录页，保留原路径以便登录后回跳。
 * 仅在客户端触发；供 useHttp 拦截器与绕过 useHttp 的直接请求（如文件下载）共用。
 */
export function handleUnauthorized() {
    if (!import.meta.client) {
        return;
    }

    try {
        useAuthStore().reset();
    } catch {
        // 忽略 Nuxt 上下文不可用的情况
    }

    if (typeof window !== "undefined" && window.location.pathname !== "/login") {
        const redirect = window.location.pathname + window.location.search;
        void navigateTo({path: "/login", query: {redirect}});
    }
}

/**
 * 处理后端返回 401（未登录 / 会话过期）。
 *
 * 仅在客户端、且非 /auth/ 接口上触发：登录接口本身的 401 表示账密错误，交由调用方处理。
 */
function redirectOnUnauthorized(error: unknown, url: string) {
    if (!import.meta.client) {
        return;
    }
    if (url.startsWith("/auth/")) {
        return;
    }

    const status = (error as { response?: { status?: number }; statusCode?: number })?.response?.status
        ?? (error as { statusCode?: number })?.statusCode;
    if (status !== 401) {
        return;
    }

    handleUnauthorized();
}

/** 读取当前登录令牌，用于在每个请求上注入 Authorization 头；上下文不可用时返回 null。 */
export function readAuthToken(): string | null {
    try {
        return useAuthStore().token;
    } catch {
        return null;
    }
}

export const useHttp = () => {
    const runtimeConfig = useRuntimeConfig();
    const apiBase = runtimeConfig.public.apiBase as string || "http://127.0.0.1:8080/api";

    const http = createFetch({
        defaults: {
            baseURL: apiBase,
            headers: {
                Accept: "application/json",
            },
            async onRequest({options}) {
                const token = readAuthToken();
                if (token) {
                    options.headers.set("Authorization", `Bearer ${token}`);
                }
            },
        },
    });

    const requestBase = async <TResponse, TPayload = Record<string, unknown>>(
        url: string,
        payload?: TPayload,
        options: HttpRequestOptions<TPayload> = {},
    ): Promise<ApiResult<TResponse>> => {
        const {payloadMode = "query", method = "GET", params, body, ...fetchOptions} = options;
        const query = payloadMode === "query"
            ? (params ?? (payload as QueryParams | undefined))
            : params;
        const requestBody = payloadMode === "json"
            ? ((body ?? payload) as JsonBody)
            : undefined;
        let response: ApiResult<TResponse>;
        try {
            response = await http<ApiResult<TResponse>>(url, {
                method,
                ...fetchOptions,
                query,
                headers: fetchOptions.headers,
                body: requestBody,
            });
        } catch (error) {
            redirectOnUnauthorized(error, url);
            throw error;
        }

        const responseCode = getResponseCode(response);
        if (responseCode !== SUCCESS_CODE && responseCode !== SUCCESS_CODE_ALTERNATE) {
            throw createError({
                statusCode: Number(responseCode) || 500,
                statusMessage: getResponseMessage(response),
                data: response,
            });
        }

        return response;
    };

    const request = async <TResponse, TPayload = Record<string, unknown>>(
        url: string,
        payload?: TPayload,
        options: HttpRequestOptions<TPayload> = {},
    ): Promise<TResponse> => {
        const response = await requestBase<TResponse, TPayload>(url, payload, options);

        return response.data;
    };

    return {
        request,
        requestRaw: requestBase,
        get: <TResponse>(url: string, params?: QueryParams, options?: Omit<HttpRequestOptions<QueryParams>, "method" | "payloadMode" | "params" | "body">) =>
            request<TResponse>(url, params, {
                ...options,
                method: "GET",
                payloadMode: "query",
            }),
        getRaw: <TResponse>(url: string, params?: QueryParams, options?: Omit<HttpRequestOptions<QueryParams>, "method" | "payloadMode" | "params" | "body">) =>
            requestBase<TResponse>(url, params, {
                ...options,
                method: "GET",
                payloadMode: "query",
            }),
        post: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            request<TResponse, TPayload>(url, payload, {
                ...options,
                method: "POST",
            }),
        postRaw: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            requestBase<TResponse, TPayload>(url, payload, {
                ...options,
                method: "POST",
            }),
        put: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            request<TResponse, TPayload>(url, payload, {
                ...options,
                method: "PUT",
            }),
        putRaw: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            requestBase<TResponse, TPayload>(url, payload, {
                ...options,
                method: "PUT",
            }),
        patch: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            request<TResponse, TPayload>(url, payload, {
                ...options,
                method: "PATCH",
            }),
        patchRaw: <TResponse, TPayload = QueryParams>(url: string, payload?: TPayload, options?: Omit<HttpRequestOptions<TPayload>, "method">) =>
            requestBase<TResponse, TPayload>(url, payload, {
                ...options,
                method: "PATCH",
            }),
        delete: <TResponse>(url: string, params?: QueryParams, options?: Omit<HttpRequestOptions<QueryParams>, "method" | "payloadMode" | "params" | "body">) =>
            request<TResponse>(url, params, {
                ...options,
                method: "DELETE",
                payloadMode: "query",
            }),
        deleteRaw: <TResponse>(url: string, params?: QueryParams, options?: Omit<HttpRequestOptions<QueryParams>, "method" | "payloadMode" | "params" | "body">) =>
            requestBase<TResponse>(url, params, {
                ...options,
                method: "DELETE",
                payloadMode: "query",
            }),
    };
};
