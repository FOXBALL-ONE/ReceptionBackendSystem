<template>
  <n-config-provider
      :hljs="hljs"
      :theme="osTheme"
      :locale="zhCN"
      :date-locale="dateZhCN"
  >
    <nuxt-loading-indicator />
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <n-layout class="app-layout">
            <n-layout-header class="app-header" bordered>
              <div class="app-header-inner">
                <NuxtLink to="/" class="brand">
                  <div class="brand-mark-wrapper">
                    <span class="brand-mark">接</span>
                  </div>
                  <span class="brand-copy">
                    <span class="brand-title">活动接待管理</span>
                    <span class="brand-subtitle">Reception Management System</span>
                  </span>
                </NuxtLink>

                <nav class="app-nav">
                  <NuxtLink to="/event" class="app-nav-link">
                    <n-icon size="18" class="nav-icon">
                      <svg viewBox="0 0 24 24">
                        <path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                      </svg>
                    </n-icon>
                    <span>主页</span>
                  </NuxtLink>
                  <NuxtLink to="/" class="app-nav-link">
                    <n-icon size="18" class="nav-icon">
                      <svg viewBox="0 0 24 24">
                        <path fill="currentColor" d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
                      </svg>
                    </n-icon>
                    <span>活动配置</span>
                  </NuxtLink>
                </nav>

                <div class="app-actions">
                  <n-dropdown trigger="hover" :options="userMenuOptions">
                    <div class="user-profile">
                      <n-avatar
                        round
                        size="small"
                        :style="{
                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                        }"
                      >
                        管
                      </n-avatar>
                      <span class="user-name">管理员</span>
                      <n-icon size="16" class="dropdown-icon">
                        <svg viewBox="0 0 24 24">
                          <path fill="currentColor" d="M7 10l5 5 5-5z"/>
                        </svg>
                      </n-icon>
                    </div>
                  </n-dropdown>
                </div>
              </div>
            </n-layout-header>

            <n-layout-content class="app-content">
              <main class="app-main">
                <slot/>
              </main>
            </n-layout-content>

            <n-layout-footer class="app-footer" bordered>
              <div class="app-footer-inner">
                <div class="footer-content">
                  <div class="footer-left">
                    <span class="footer-brand">活动接待管理系统</span>
                    <span class="footer-divider">|</span>
                    <span class="footer-location">成府路</span>
                  </div>
                  <div class="footer-right">
                    <span class="footer-copyright">© 2024 All Rights Reserved</span>
                  </div>
                </div>
              </div>
            </n-layout-footer>
          </n-layout>
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>
<script setup lang="ts">
import { dateZhCN, darkTheme, useOsTheme, zhCN } from "naive-ui";

import hljs from "highlight.js/lib/core";
import ini from "highlight.js/lib/languages/ini";
import json from "highlight.js/lib/languages/json";
import yaml from "highlight.js/lib/languages/yaml";

hljs.registerLanguage("json", json);
hljs.registerLanguage("ini", ini);
hljs.registerLanguage("toml", ini);
hljs.registerLanguage("yaml", yaml);

const naiveOsTheme = useOsTheme();

const osTheme = computed(() => (naiveOsTheme.value === "dark" ? darkTheme : null));

const userMenuOptions = [
  {
    label: '个人信息',
    key: 'profile',
    icon: () => h('svg', { viewBox: '0 0 24 24', style: 'width: 18px; height: 18px;' }, [
      h('path', { fill: 'currentColor', d: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z' })
    ])
  },
  {
    label: '系统设置',
    key: 'settings',
    icon: () => h('svg', { viewBox: '0 0 24 24', style: 'width: 18px; height: 18px;' }, [
      h('path', { fill: 'currentColor', d: 'M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14.4 2.81a.488.488 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z' })
    ])
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h('svg', { viewBox: '0 0 24 24', style: 'width: 18px; height: 18px;' }, [
      h('path', { fill: 'currentColor', d: 'M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z' })
    ])
  }
];

</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.app-header-inner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 32px;
  max-width: 1400px;
  min-height: 68px;
  margin: 0 auto;
  padding: 0 32px;
}

/* 品牌区域 */
.brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: inherit;
  text-decoration: none;
  transition: transform 0.2s ease;
}

.brand:hover {
  transform: translateY(-1px);
}

.brand-mark-wrapper {
  position: relative;
}

.brand-mark-wrapper::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.brand:hover .brand-mark-wrapper::before {
  opacity: 0.15;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.brand:hover .brand-mark {
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
  transform: scale(1.05);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.2;
}

.brand-title {
  font-size: 17px;
  font-weight: 650;
  color: #1a1a1a;
  letter-spacing: -0.2px;
}

.brand-subtitle {
  color: #8b92a7;
  font-size: 12px;
  font-weight: 450;
  letter-spacing: 0.2px;
}

/* 导航区域 */
.app-nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 6px;
  border: 1px solid rgba(228, 231, 236, 0.8);
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.6);
  backdrop-filter: blur(10px);
}

.app-nav-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 100px;
  height: 38px;
  padding: 0 18px;
  border-radius: 9px;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-icon {
  transition: transform 0.25s ease;
}

.app-nav-link:hover {
  color: #475569;
  background: rgba(255, 255, 255, 0.8);
}

.app-nav-link:hover .nav-icon {
  transform: scale(1.1);
}

.app-nav-link.router-link-active {
  color: #6366f1;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.15), 0 1px 2px rgba(0, 0, 0, 0.06);
}

.app-nav-link.router-link-active::before {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

/* 用户操作区域 */
.app-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px 6px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(248, 250, 252, 0.6);
  border: 1px solid rgba(228, 231, 236, 0.6);
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.dropdown-icon {
  color: #94a3b8;
  transition: transform 0.2s ease;
}

.user-profile:hover .dropdown-icon {
  transform: translateY(1px);
}

/* 内容区域 */
.app-content {
  min-height: calc(100vh - 68px - 48px);
}

.app-main {
  max-width: 1400px;
  margin: 0 auto;
}

/* 页脚 */
.app-footer {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(228, 231, 236, 0.6);
}

.app-footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 14px 32px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #64748b;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-brand {
  font-weight: 550;
  color: #475569;
}

.footer-divider {
  color: #cbd5e1;
}

.footer-location {
  color: #64748b;
}

.footer-right {
  color: #94a3b8;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .app-header-inner {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 12px 20px;
  }

  .app-nav {
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
  }

  .app-nav-link {
    flex: 0 0 auto;
  }

  .app-actions {
    justify-content: flex-start;
  }

  .app-main {
    padding: 0;
  }
}

@media (max-width: 640px) {
  .brand-subtitle {
    display: none;
  }

  .app-nav {
    gap: 4px;
    padding: 4px;
  }

  .app-nav-link {
    min-width: auto;
    padding: 0 12px;
    font-size: 13px;
  }

  .app-nav-link span {
    display: none;
  }

  .footer-content {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }

  .footer-left {
    flex-direction: column;
    gap: 4px;
  }

  .footer-divider {
    display: none;
  }
}
</style>
