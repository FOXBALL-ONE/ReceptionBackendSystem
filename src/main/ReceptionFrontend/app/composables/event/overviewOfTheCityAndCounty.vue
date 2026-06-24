<template>
  <n-spin :show="loading">
    <n-space vertical :size="16" class="overview-editor">
    <n-card title="市县概况" :bordered="false">
      <n-alert v-if="saveStatus.message" :type="saveStatus.type" class="status-alert" :show-icon="false">
        {{ saveStatus.message }}
      </n-alert>

      <n-form label-placement="top" :show-feedback="false">
        <n-grid cols="1 m:2" :x-gap="16" :y-gap="14" responsive="screen">
          <n-form-item-gi label="标题">
            <n-input v-model:value="overview.title" placeholder="请输入标题" clearable />
          </n-form-item-gi>
          <n-form-item-gi label="顶部图片">
            <div class="image-url-control">
              <n-input v-model:value="overview.heroImageUrl" placeholder="图片 URL" clearable />
              <label class="upload-control" :class="{ disabled: uploading }">
                <GoUpload />
                {{ uploading ? "上传中" : "上传" }}
                <input type="file" accept="image/*" :disabled="uploading" @change="handleHeroImageChange">
              </label>
              <n-button
                circle
                quaternary
                type="error"
                :disabled="!overview.heroImageUrl"
                aria-label="删除顶部图片"
                @click="removeHeroImage"
              >
                <template #icon>
                  <GoX />
                </template>
              </n-button>
            </div>
          </n-form-item-gi>
        </n-grid>
      </n-form>

      <div v-if="overview.heroImageUrl" class="hero-preview">
        <img :src="overview.heroImageUrl" :alt="overview.title || '顶部图片'">
      </div>
    </n-card>

    <n-card title="段落内容" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addParagraph">
          <template #icon>
            <GoPlus />
          </template>
          增加段落
        </n-button>
      </template>

      <n-empty v-if="paragraphs.length === 0" description="暂无段落">
        <template #extra>
          <n-button type="primary" @click="addParagraph">增加段落</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="14">
        <section v-for="(paragraph, index) in paragraphs" :key="paragraph.id" class="paragraph-panel">
          <div class="paragraph-header">
            <div class="paragraph-title">
              <span class="paragraph-order">段落 {{ index + 1 }}</span>
              <span class="paragraph-summary">{{ getParagraphSummary(paragraph) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="index === 0"
                aria-label="上移段落"
                @click="moveParagraph(index, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="index === paragraphs.length - 1"
                aria-label="下移段落"
                @click="moveParagraph(index, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除段落" @click="removeParagraph(index)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1" :y-gap="12" responsive="screen">
              <n-form-item-gi label="段落标题">
                <n-input v-model:value="paragraph.title" placeholder="请输入段落标题" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="段落内容">
                <n-input
                  v-model:value="paragraph.content"
                  type="textarea"
                  placeholder="请输入段落内容"
                  :autosize="{ minRows: 5, maxRows: 12 }"
                  clearable
                />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </section>
      </n-space>
    </n-card>

    <n-flex justify="end" class="footer-actions">
      <n-button secondary @click="addParagraph">
        <template #icon>
          <GoPlus />
        </template>
        增加段落
      </n-button>
      <n-button type="primary" :loading="saving" @click="saveOverview">保存概况</n-button>
    </n-flex>
    </n-space>
  </n-spin>
</template>

<script setup lang="ts">
import type { Ref } from "vue";
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash, GoUpload, GoX } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>("eventId");
const eventApi = useEventApi();
const message = useMessage();

interface OverviewContent {
  title: string;
  heroImageUrl: string;
}

interface OverviewParagraph {
  id: string;
  title: string;
  content: string;
}

interface OverviewPayload {
  id?: number | string | null;
  title?: string | null;
  topImageUrl?: string | null;
  heroImageUrl?: string | null;
  description?: Array<{
    title?: string | null;
    content?: string | null;
  }> | null;
}

interface SaveStatus {
  type: "success" | "warning" | "error" | "info";
  message: string;
}

const idCounters = {
  paragraph: 1,
};

const objectUrls = new Set<string>();

const overview = ref<OverviewContent>({
  title: "",
  heroImageUrl: "",
});
const paragraphs = ref<OverviewParagraph[]>([createParagraph()]);
const overviewId = ref<number | string | null>(null);
const loading = ref(false);
const saving = ref(false);
const uploading = ref(false);
const saveStatus = ref<SaveStatus>({
  type: "info",
  message: "",
});

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createParagraph(values: Partial<Omit<OverviewParagraph, "id">> = {}): OverviewParagraph {
  return {
    id: nextId("paragraph"),
    title: values.title ?? "",
    content: values.content ?? "",
  };
}

function resetOverviewState() {
  overviewId.value = null;
  overview.value = {
    title: "",
    heroImageUrl: "",
  };
  paragraphs.value = [createParagraph()];
}

function addParagraph() {
  paragraphs.value.push(createParagraph());
  saveStatus.value.message = "";
}

function removeParagraph(index: number) {
  paragraphs.value.splice(index, 1);
  saveStatus.value.message = "";
}

function moveItem<T>(items: T[], index: number, offset: -1 | 1) {
  const targetIndex = index + offset;
  if (targetIndex < 0 || targetIndex >= items.length) {
    return;
  }

  const [item] = items.splice(index, 1);
  items.splice(targetIndex, 0, item);
}

function moveParagraph(index: number, offset: -1 | 1) {
  moveItem(paragraphs.value, index, offset);
  saveStatus.value.message = "";
}

function getParagraphSummary(paragraph: OverviewParagraph) {
  const summaryParts = [paragraph.title, paragraph.content].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写段落内容";
}

function revokeObjectUrl(url: string) {
  if (!url.startsWith("blob:") || !objectUrls.has(url)) {
    return;
  }

  if (typeof URL !== "undefined" && typeof URL.revokeObjectURL === "function") {
    URL.revokeObjectURL(url);
  }

  objectUrls.delete(url);
}

function pickUploadUrl(response: unknown): string {
  if (!response) {
    return "";
  }

  if (Array.isArray(response)) {
    return pickUploadUrl(response[0]);
  }

  if (typeof response !== "object") {
    return "";
  }

  const record = response as Record<string, any>;

  return String(
    record.accessUrl
    ?? record.url
    ?? record.fileUrl
    ?? record.data?.accessUrl
    ?? record.data?.url
    ?? record.data?.fileUrl
    ?? "",
  );
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

async function handleHeroImageChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];

  if (!file) {
    input.value = "";
    return;
  }

  if (!eventId?.value) {
    input.value = "";
    saveStatus.value = {
      type: "warning",
      message: "请先选择活动",
    };
    return;
  }

  try {
    uploading.value = true;
    const response = await eventApi.uploadOverviewImage(file, eventId.value);
    const uploadedUrl = pickUploadUrl(response);

    if (!uploadedUrl) {
      saveStatus.value = {
        type: "warning",
        message: "图片已上传，但接口未返回访问地址",
      };
      return;
    }

    revokeObjectUrl(overview.value.heroImageUrl);
    overview.value.heroImageUrl = uploadedUrl;
    saveStatus.value.message = "";
    message.success("顶部图片上传成功");
  } catch (error: unknown) {
    saveStatus.value = {
      type: "error",
      message: getErrorMessage(error, "顶部图片上传失败"),
    };
  } finally {
    uploading.value = false;
    input.value = "";
  }
}

function removeHeroImage() {
  revokeObjectUrl(overview.value.heroImageUrl);
  overview.value.heroImageUrl = "";
  saveStatus.value.message = "";
}

function pickOverview(response: unknown): OverviewPayload | null {
  if (Array.isArray(response)) {
    return (response[0] as OverviewPayload | undefined) ?? null;
  }

  if (!response || typeof response !== "object") {
    return null;
  }

  const record = response as Record<string, unknown>;
  const listKeys = ["list", "records", "rows", "items", "data"] as const;

  for (const key of listKeys) {
    const value = record[key];
    if (Array.isArray(value)) {
      return (value[0] as OverviewPayload | undefined) ?? null;
    }
  }

  return response as OverviewPayload;
}

function assignOverview(payload: OverviewPayload | null) {
  if (!payload) {
    resetOverviewState();
    return;
  }

  overviewId.value = payload.id ?? null;
  overview.value = {
    title: String(payload.title ?? ""),
    heroImageUrl: String(payload.topImageUrl ?? payload.heroImageUrl ?? ""),
  };

  const description = Array.isArray(payload.description) ? payload.description : [];
  paragraphs.value = description.length > 0
    ? description.map((paragraph) => createParagraph({
      title: String(paragraph.title ?? ""),
      content: String(paragraph.content ?? ""),
    }))
    : [createParagraph()];
}

function buildSavePayload() {
  return {
    ...(overviewId.value ? { id: overviewId.value } : {}),
    title: overview.value.title.trim(),
    topImageUrl: overview.value.heroImageUrl.trim(),
    description: paragraphs.value.map((paragraph) => ({
      title: paragraph.title.trim(),
      content: paragraph.content.trim(),
    })),
  };
}

function hasOverviewContent() {
  return Boolean(
    overview.value.title.trim()
    || overview.value.heroImageUrl.trim()
    || paragraphs.value.some((paragraph) => paragraph.title.trim() || paragraph.content.trim()),
  );
}

async function loadOverviewData() {
  if (!eventId?.value) {
    resetOverviewState();
    return;
  }

  try {
    loading.value = true;
    const data = await eventApi.getOverviewsByActivity(eventId.value);
    assignOverview(pickOverview(data));
  } catch (error: unknown) {
    resetOverviewState();
    saveStatus.value = {
      type: "error",
      message: getErrorMessage(error, "加载市县概况失败"),
    };
  } finally {
    loading.value = false;
  }
}

async function saveOverview() {
  if (!eventId?.value) {
    saveStatus.value = {
      type: "warning",
      message: "请先选择活动",
    };
    return;
  }

  if (!hasOverviewContent()) {
    saveStatus.value = {
      type: "warning",
      message: "请先填写概况内容",
    };
    return;
  }

  try {
    saving.value = true;
    const payload = buildSavePayload();
    const response = await eventApi.saveOverviews(eventId.value, [payload]);
    const savedOverview = pickOverview(response);

    if (savedOverview?.id) {
      overviewId.value = savedOverview.id;
    }

    saveStatus.value = {
      type: "success",
      message: "市县概况已保存",
    };
  } catch (error: unknown) {
    saveStatus.value = {
      type: "error",
      message: getErrorMessage(error, "保存市县概况失败"),
    };
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadOverviewData();
});

watch(() => eventId?.value, () => {
  void loadOverviewData();
});

onBeforeUnmount(() => {
  objectUrls.forEach((url) => {
    if (typeof URL !== "undefined" && typeof URL.revokeObjectURL === "function") {
      URL.revokeObjectURL(url);
    }
  });
  objectUrls.clear();
});
</script>

<style scoped>
.overview-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.status-alert {
  margin-bottom: 12px;
}

.image-url-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  align-items: center;
}

.upload-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid #d0d5dd;
  border-radius: 6px;
  color: #344054;
  font-size: 14px;
  line-height: 1;
  background: #ffffff;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease, background-color 0.18s ease;
}

.upload-control:hover {
  border-color: #2563eb;
  color: #175cd3;
  background: #f8fbff;
}

.upload-control.disabled {
  opacity: 0.62;
  cursor: not-allowed;
  pointer-events: none;
}

.upload-control input {
  display: none;
}

.hero-preview {
  overflow: hidden;
  width: 100%;
  margin-top: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
  aspect-ratio: 16 / 6;
}

.hero-preview img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.paragraph-panel {
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.paragraph-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.paragraph-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.paragraph-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.paragraph-summary {
  min-width: 0;
  overflow: hidden;
  color: #475467;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.footer-actions {
  padding-bottom: 4px;
}

@media (max-width: 720px) {
  .image-url-control {
    grid-template-columns: 1fr;
  }

  .hero-preview {
    aspect-ratio: 16 / 9;
  }

  .paragraph-header,
  .paragraph-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .paragraph-title,
  .paragraph-summary {
    width: 100%;
  }

  .paragraph-summary {
    white-space: normal;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
