// https://nuxt.com/docs/api/configuration/nuxt-config
import AutoImport from "unplugin-auto-import/vite";
import {NaiveUiResolver} from "unplugin-vue-components/resolvers";
import Components from "unplugin-vue-components/vite";

export default defineNuxtConfig({
      ssr: true,
      compatibilityDate: '2025-07-15',
      runtimeConfig: {
        public: {
          apiBase: process.env.NUXT_PUBLIC_API_BASE || "http://127.0.0.1:8080/api",
        },
      },
      routeRules: {
        // 全局启用登录校验中间件（middleware/auth.ts）
        "/**": {
          appMiddleware: {
            auth: true,
          },
        },
      },
      devtools: {
        enabled: true,

        timeline: {
          enabled: true
        }
      },
      modules: [
        "@bg-dev/nuxt-naiveui",
        "@pinia/nuxt",
        ["pinia-plugin-persistedstate/nuxt", {
          storage: "localStorage",
        }],
        "dayjs-nuxt",
      ],
      vite: {
        optimizeDeps: {
          include: [
            "highlight.js/lib/core",
            "highlight.js/lib/languages/json",
            "highlight.js/lib/languages/ini",
            "highlight.js/lib/languages/yaml",
            "@vue/devtools-core",
            "@vue/devtools-kit",
            "dayjs",
            "dayjs/plugin/updateLocale",
            "dayjs/locale/zh-cn",
            "dayjs/plugin/timezone",
            "dayjs/plugin/localizedFormat",
            "dayjs/plugin/relativeTime",
            "dayjs/plugin/utc",
          ],
        },
        plugins: [
          AutoImport({
            imports: [
              {
                "naive-ui": [
                  "useDialog",
                  "useMessage",
                  "useNotification",
                  "useLoadingBar",
                ],
              },
            ],
          }),
          Components({
            resolvers: [NaiveUiResolver()],
          }),
        ],
      },
      dayjs: {
        locales: ["zh-cn"],
        defaultLocale: "zh-cn",
        plugins: ["timezone", "localizedFormat"],
        defaultTimezone: "Asia/Shanghai",
      },
      devServer: {
        host: "0.0.0.0",
        port: 8040,
      },
    }
)
