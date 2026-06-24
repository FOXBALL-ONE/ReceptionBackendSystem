<template>
  <n-space vertical :size="16" class="inspection-points-editor">
    <n-card title="考察点" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addInspectionPoint">
          <template #icon>
            <GoPlus />
          </template>
          添加考察点
        </n-button>
      </template>

      <n-alert v-if="saveStatus.message" :type="saveStatus.type" class="status-alert" :show-icon="false">
        {{ saveStatus.message }}
      </n-alert>

      <n-spin :show="loading">
        <n-empty v-if="inspectionPoints.length === 0" description="暂无考察点">
          <template #extra>
            <n-button type="primary" @click="addInspectionPoint">添加考察点</n-button>
          </template>
        </n-empty>

        <n-space v-else vertical :size="14">
          <section v-for="(point, index) in inspectionPoints" :key="point.id" class="inspection-panel">
            <div class="inspection-header">
              <div class="inspection-title">
                <span class="inspection-order">考察点 {{ index + 1 }}</span>
                <span class="inspection-summary">{{ getInspectionSummary(point) }}</span>
              </div>
              <n-flex :size="4" align="center">
                <n-button
                  circle
                  quaternary
                  :disabled="index === 0"
                  aria-label="上移考察点"
                  @click="moveInspectionPoint(index, -1)"
                >
                  <template #icon>
                    <GoArrowUp />
                  </template>
                </n-button>
                <n-button
                  circle
                  quaternary
                  :disabled="index === inspectionPoints.length - 1"
                  aria-label="下移考察点"
                  @click="moveInspectionPoint(index, 1)"
                >
                  <template #icon>
                    <GoArrowDown />
                  </template>
                </n-button>
                <n-button circle quaternary type="error" aria-label="删除考察点" @click="removeInspectionPoint(index)">
                  <template #icon>
                    <GoTrash />
                  </template>
                </n-button>
              </n-flex>
            </div>

            <n-form label-placement="top" :show-feedback="false">
              <n-grid cols="1 m:2" :x-gap="16" :y-gap="14" responsive="screen">
                <n-form-item-gi label="考察点名称">
                  <n-input v-model:value="point.name" placeholder="请输入考察点名称" clearable />
                </n-form-item-gi>
                <n-form-item-gi label="图片">
                  <div class="image-url-control">
                    <n-input v-model:value="point.imageUrl" placeholder="图片 URL" clearable />
                    <label class="upload-control">
                      <GoUpload />
                      上传
                      <input type="file" accept="image/*" @change="handleLocalImageChange($event, point)">
                    </label>
                    <n-button
                      circle
                      quaternary
                      type="error"
                      :disabled="!point.imageUrl"
                      aria-label="删除图片"
                      @click="removePointImage(point)"
                    >
                      <template #icon>
                        <GoX />
                      </template>
                    </n-button>
                  </div>
                </n-form-item-gi>
                <n-form-item-gi label="介绍" class="intro-field">
                  <n-input
                    v-model:value="point.introduction"
                    type="textarea"
                    placeholder="请输入考察点介绍"
                    :autosize="{ minRows: 4, maxRows: 8 }"
                    clearable
                  />
                </n-form-item-gi>
              </n-grid>
            </n-form>

            <div v-if="point.imageUrl" class="image-preview">
              <img :src="point.imageUrl" :alt="point.name || '考察点图片'">
            </div>
          </section>
        </n-space>
      </n-spin>
    </n-card>

    <n-flex justify="end" class="footer-actions">
      <n-button secondary @click="addInspectionPoint">
        <template #icon>
          <GoPlus />
        </template>
        添加考察点
      </n-button>
      <n-button type="primary" :loading="saving" @click="saveInspectionPoints">保存考察点</n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash, GoUpload, GoX } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface InspectionPoint {
  id: string;
  backendId: number | null;
  name: string;
  imageUrl: string;
  introduction: string;
  localFile: File | null;
}

interface SaveStatus {
  type: "success" | "warning" | "error" | "info";
  message: string;
}

const idCounters = {
  point: 1,
};

const objectUrls = new Set<string>();

const inspectionPoints = ref<InspectionPoint[]>([createInspectionPoint()]);
const saveStatus = ref<SaveStatus>({
  type: "info",
  message: "",
});
const loading = ref(false);
const saving = ref(false);

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createInspectionPoint(values: Partial<Omit<InspectionPoint, "id">> = {}): InspectionPoint {
  return {
    id: nextId("point"),
    backendId: values.backendId ?? null,
    name: values.name ?? "",
    imageUrl: values.imageUrl ?? "",
    introduction: values.introduction ?? "",
    localFile: values.localFile ?? null,
  };
}

function addInspectionPoint() {
  inspectionPoints.value.push(createInspectionPoint());
  saveStatus.value.message = "";
}

function removeInspectionPoint(index: number) {
  cleanupPointUrl(inspectionPoints.value[index]);
  inspectionPoints.value.splice(index, 1);
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

function moveInspectionPoint(index: number, offset: -1 | 1) {
  moveItem(inspectionPoints.value, index, offset);
  saveStatus.value.message = "";
}

function getInspectionSummary(point: InspectionPoint) {
  const summaryParts = [point.name || point.introduction, point.imageUrl ? "已设置图片" : ""].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写考察点信息";
}

function pickList(response: any, ...keys: string[]): any[] {
  if (Array.isArray(response)) return response;

  for (const key of keys) {
    if (Array.isArray(response?.[key])) {
      return response[key];
    }
  }

  if (response?.data && response.data !== response) {
    return pickList(response.data, ...keys);
  }

  return [];
}

function normalizeBackendId(value: unknown) {
  const id = Number(value);

  return Number.isFinite(id) ? id : null;
}

function normalizeInspectionPoints(response: any): InspectionPoint[] {
  return pickList(response, "records", "rows", "list", "items", "inspectionPoints", "data")
    .map((point) => createInspectionPoint({
      backendId: normalizeBackendId(point?.id),
      imageUrl: String(point?.imageURL ?? "").trim(),
      introduction: String(point?.description ?? "").trim(),
    }));
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

function pickUploadUrl(response: any): string {
  if (!response) {
    return "";
  }

  return String(
    response.accessUrl ??
    response.url ??
    response.fileUrl ??
    response.data?.accessUrl ??
    response.data?.url ??
    "",
  );
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

function cleanupPointUrl(point?: InspectionPoint) {
  if (!point) {
    return;
  }

  revokeObjectUrl(point.imageUrl);
}

function handleLocalImageChange(event: Event, point: InspectionPoint) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file || typeof URL === "undefined" || typeof URL.createObjectURL !== "function") {
    return;
  }

  revokeObjectUrl(point.imageUrl);
  const url = URL.createObjectURL(file);
  objectUrls.add(url);
  point.imageUrl = url;
  point.localFile = file;
  input.value = "";
  saveStatus.value.message = "";
}

function removePointImage(point: InspectionPoint) {
  revokeObjectUrl(point.imageUrl);
  point.imageUrl = "";
  point.localFile = null;
  saveStatus.value.message = "";
}

async function uploadPendingPointImages() {
  if (!eventId?.value) return

  for (const point of inspectionPoints.value) {
    if (!point.localFile) {
      continue;
    }

    const previewUrl = point.imageUrl;
    const response = await eventApi.uploadInspectionPointImage(point.localFile, eventId.value);
    const uploadedUrl = pickUploadUrl(response);

    if (!uploadedUrl) {
      throw new Error("图片已上传，但接口未返回访问地址");
    }

    point.imageUrl = uploadedUrl;
    point.localFile = null;
    revokeObjectUrl(previewUrl);
  }
}

function buildSavePayload() {
  return {
    inspectionPoints: inspectionPoints.value
      .map((point) => {
        const imageURL = point.imageUrl.startsWith("blob:") ? "" : point.imageUrl.trim();
        const description = point.introduction.trim() || point.name.trim();

        if (!point.backendId && !imageURL && !description) {
          return null;
        }

        return {
          ...(point.backendId ? { id: point.backendId } : {}),
          imageURL,
          description,
        };
      })
      .filter((point): point is NonNullable<typeof point> => point !== null),
  };
}

async function loadInspectionPointsData() {
  if (!eventId?.value) return

  try {
    loading.value = true
    const response = await eventApi.getInspectionPointsByActivity(eventId.value)
    const loadedPoints = normalizeInspectionPoints(response)

    inspectionPoints.value = loadedPoints.length > 0 ? loadedPoints : [createInspectionPoint()]
    saveStatus.value.message = ""
  } catch (error) {
    message.error(getErrorMessage(error, "加载考察点数据失败"))
    inspectionPoints.value = [createInspectionPoint()]
  } finally {
    loading.value = false
  }
}

async function saveInspectionPoints() {
  if (!eventId?.value) {
    saveStatus.value = {
      type: "warning",
      message: "请先选择活动",
    };
    return;
  }

  try {
    saving.value = true
    saveStatus.value = { type: "info", message: "" }

    await uploadPendingPointImages()
    const payload = buildSavePayload();
    await eventApi.saveInspectionPoints(eventId.value, payload.inspectionPoints)
    await loadInspectionPointsData()

    saveStatus.value = {
      type: "success",
      message: "考察点已保存",
    };
  } catch (error) {
    saveStatus.value = {
      type: "error",
      message: getErrorMessage(error, "考察点保存失败"),
    };
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (eventId?.value) {
    loadInspectionPointsData()
  }
})

watch(() => eventId?.value, (newId) => {
  if (newId) {
    loadInspectionPointsData()
  }
})

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
.inspection-points-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.status-alert {
  margin-bottom: 12px;
}

.inspection-panel {
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.inspection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.inspection-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.inspection-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.inspection-summary {
  min-width: 0;
  overflow: hidden;
  color: #475467;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.upload-control input {
  display: none;
}

.intro-field {
  grid-column: 1 / -1;
}

.image-preview {
  overflow: hidden;
  width: 100%;
  max-width: 360px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
  aspect-ratio: 16 / 9;
}

.image-preview img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.footer-actions {
  padding-bottom: 4px;
}

@media (max-width: 720px) {
  .inspection-header,
  .inspection-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .inspection-title,
  .inspection-summary {
    width: 100%;
  }

  .inspection-summary {
    white-space: normal;
  }

  .image-url-control {
    grid-template-columns: 1fr;
  }

  .image-preview {
    max-width: 100%;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
