<template>
  <div class="access-control">
    <n-spin :show="loading">
      <n-space vertical :size="16">
        <n-card :bordered="false" class="access-card">
          <div class="access-header">
            <div class="status-icon" :class="open ? 'is-open' : 'is-closed'">
              <component :is="open ? GoUnlock : GoLock" />
            </div>
            <div class="access-title-block">
              <div class="access-kicker">访问控制</div>
              <h2 class="access-title">{{ statusTitle }}</h2>
              <p class="access-description">{{ statusDescription }}</p>
            </div>
            <n-tag :type="open ? 'success' : 'warning'" round>
              {{ open ? "已开放" : "已关闭" }}
            </n-tag>
          </div>
        </n-card>

        <n-card :bordered="false" class="settings-card">
          <div class="setting-row">
            <div class="setting-copy">
              <div class="setting-title">访客访问状态</div>
              <div class="setting-description">
                开启后访客可正常进入活动页面，关闭后访客将看到关闭提示页面。
              </div>
            </div>
            <n-switch
              v-model:value="open"
              size="large"
              :disabled="loading || saving"
              aria-label="切换活动访问状态"
            >
              <template #checked>开放</template>
              <template #unchecked>关闭</template>
            </n-switch>
          </div>

          <n-divider />

          <div class="preview-panel" :class="open ? 'is-open' : 'is-closed'">
            <div class="preview-icon">
              <component :is="open ? GoEye : GoEyeClosed" />
            </div>
            <div class="preview-content">
              <div class="preview-title">{{ previewTitle }}</div>
              <div class="preview-description">{{ previewDescription }}</div>
            </div>
          </div>
        </n-card>

        <n-card :bordered="false" class="info-card">
          <n-grid cols="1 s:2" :x-gap="16" :y-gap="12" responsive="screen">
            <n-gi>
              <div class="info-item">
                <span class="info-label">活动名称</span>
                <span class="info-value">{{ eventName || "未读取到活动名称" }}</span>
              </div>
            </n-gi>
            <n-gi>
              <div class="info-item">
                <span class="info-label">访问路径</span>
                <span class="info-value">{{ eventUrl || "未配置访问路径" }}</span>
              </div>
            </n-gi>
          </n-grid>
        </n-card>

        <n-alert v-if="dirty" type="info" :show-icon="false" class="status-alert">
          访问状态已修改，保存后才会同步到访客页面。
        </n-alert>

        <n-flex justify="end" :size="12" class="footer-actions">
          <n-button :disabled="loading || saving" @click="loadAccessState">
            <template #icon>
              <GoSync />
            </template>
            刷新状态
          </n-button>
          <n-button type="primary" :disabled="!dirty || loading" :loading="saving" @click="saveAccessState">
            <template #icon>
              <GoCheck />
            </template>
            保存访问设置
          </n-button>
        </n-flex>
      </n-space>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import type { Ref } from "vue";
import type { Event } from "~/types/event";
import { GoCheck, GoEye, GoEyeClosed, GoLock, GoSync, GoUnlock } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>("eventId");
const eventApi = useEventApi();
const message = useMessage();

const loading = ref(false);
const saving = ref(false);
const open = ref(false);
const originalOpen = ref(false);
const eventName = ref("");
const eventUrl = ref("");

const dirty = computed(() => open.value !== originalOpen.value);
const statusTitle = computed(() => (open.value ? "页面开放中" : "页面已关闭"));
const statusDescription = computed(() =>
  open.value
    ? "访客现在可以访问该活动页面。"
    : "访客暂时无法访问该活动页面。",
);
const previewTitle = computed(() => (open.value ? "访客将进入活动内容页" : "访客将看到关闭提示页"));
const previewDescription = computed(() =>
  open.value
    ? "适用于活动正式发布、现场签到或对外展示阶段。"
    : "适用于内容未完善、活动结束或需要临时隐藏的阶段。",
);

function normalizeBoolean(value: unknown, fallback: boolean) {
  return typeof value === "boolean" ? value : fallback;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  if (typeof error === "object" && error !== null && "statusMessage" in error) {
    const statusMessage = (error as { statusMessage?: unknown }).statusMessage;
    if (typeof statusMessage === "string" && statusMessage) {
      return statusMessage;
    }
  }

  return fallback;
}

function assignEvent(event: Partial<Event>) {
  const nextOpen = normalizeBoolean(event.isOpen, false);

  open.value = nextOpen;
  originalOpen.value = nextOpen;
  eventName.value = event.name ?? eventName.value;
  eventUrl.value = event.url ?? eventUrl.value;
}

async function loadAccessState() {
  if (!eventId?.value) {
    message.warning("请先选择活动");
    return;
  }

  try {
    loading.value = true;
    const event = await eventApi.getEventDetail(eventId.value);
    assignEvent(event);
  } catch (error) {
    message.error(getErrorMessage(error, "加载访问状态失败"));
  } finally {
    loading.value = false;
  }
}

async function saveAccessState() {
  if (!eventId?.value) {
    message.warning("请先选择活动");
    return;
  }

  try {
    saving.value = true;
    const event = await eventApi.updateEventOpenStatus(eventId.value, open.value);

    if (event && typeof event === "object") {
      assignEvent({
        ...event,
        isOpen: normalizeBoolean(event.isOpen, open.value),
      });
    } else {
      originalOpen.value = open.value;
    }

    message.success(open.value ? "页面已开放" : "页面已关闭");
  } catch (error) {
    message.error(getErrorMessage(error, "保存访问设置失败"));
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (eventId?.value) {
    void loadAccessState();
  }
});

watch(() => eventId?.value, (newId) => {
  if (newId) {
    void loadAccessState();
  } else {
    open.value = false;
    originalOpen.value = false;
    eventName.value = "";
    eventUrl.value = "";
  }
});
</script>

<style scoped>
.access-control {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.access-card,
.settings-card,
.info-card {
  overflow: hidden;
}

.access-header {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
}

.status-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 8px;
  font-size: 25px;
}

.status-icon.is-open {
  color: #067647;
  background: #ecfdf3;
}

.status-icon.is-closed {
  color: #b42318;
  background: #fff1f3;
}

.access-title-block {
  min-width: 0;
}

.access-kicker {
  margin-bottom: 4px;
  color: #667085;
  font-size: 13px;
  font-weight: 600;
}

.access-title {
  margin: 0;
  color: #101828;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
}

.access-description {
  margin: 6px 0 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.6;
}

.setting-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
}

.setting-copy {
  min-width: 0;
}

.setting-title {
  color: #101828;
  font-size: 16px;
  font-weight: 650;
}

.setting-description {
  margin-top: 6px;
  color: #667085;
  font-size: 14px;
  line-height: 1.6;
}

.preview-panel {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.preview-panel.is-open {
  border-color: #abefc6;
  background: #f6fef9;
}

.preview-panel.is-closed {
  border-color: #fedf89;
  background: #fffcf5;
}

.preview-icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  color: #475467;
  font-size: 19px;
}

.preview-title {
  color: #182230;
  font-size: 15px;
  font-weight: 650;
}

.preview-description {
  margin-top: 4px;
  color: #667085;
  font-size: 13px;
  line-height: 1.5;
}

.info-item {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.info-label {
  color: #667085;
  font-size: 13px;
}

.info-value {
  min-width: 0;
  overflow-wrap: anywhere;
  color: #101828;
  font-size: 15px;
  font-weight: 600;
}

.status-alert {
  margin-top: -4px;
}

.footer-actions {
  padding-bottom: 4px;
}

@media (max-width: 720px) {
  .access-header,
  .setting-row,
  .preview-panel {
    grid-template-columns: 1fr;
  }

  .access-header {
    align-items: flex-start;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
