<template>
  <div class="basic-settings">
    <n-spin :show="loading">
      <n-form :model="formData" label-placement="top" :show-feedback="false" size="large">
        <n-space vertical :size="24">
          <n-card title="路径配置" :bordered="false" class="settings-card">
            <n-grid cols="1 s:2" :x-gap="24" :y-gap="16" responsive="screen">
              <n-form-item-gi label="页面路径" required>
                <n-input
                  v-model:value="formData.url"
                  placeholder="例如 /event/2026-conference"
                  clearable
                />
              </n-form-item-gi>
              <n-form-item-gi label="活动名称" required>
                <n-input
                  v-model:value="formData.name"
                  placeholder="例如 2026年度技术交流会"
                  clearable
                />
              </n-form-item-gi>
            </n-grid>
          </n-card>

          <n-card title="页面展示" :bordered="false" class="settings-card">
            <n-grid cols="1" :y-gap="16">
              <n-form-item-gi label="顶部主标题" required>
                <n-input
                  v-model:value="formData.masterTitle"
                  placeholder="请输入页面主标题"
                  clearable
                  maxlength="50"
                  show-count
                />
              </n-form-item-gi>
              <n-form-item-gi label="顶部副标题">
                <n-input
                  v-model:value="formData.subtitle"
                  placeholder="请输入副标题（可选）"
                  clearable
                  maxlength="100"
                  show-count
                />
              </n-form-item-gi>
            </n-grid>

            <n-divider style="margin: 20px 0" />

            <n-grid cols="1 s:2" :x-gap="24" :y-gap="16" responsive="screen">
              <n-form-item-gi label="开始时间">
                <n-date-picker
                  v-model:formatted-value="formData.startTime"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm"
                  value-format="yyyy-MM-dd HH:mm"
                  placeholder="请选择开始时间"
                  clearable
                  style="width: 100%"
                />
              </n-form-item-gi>
              <n-form-item-gi label="结束时间">
                <n-date-picker
                  v-model:formatted-value="formData.endTime"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm"
                  value-format="yyyy-MM-dd HH:mm"
                  placeholder="请选择结束时间"
                  clearable
                  style="width: 100%"
                />
              </n-form-item-gi>
            </n-grid>

            <n-divider style="margin: 20px 0" />

            <n-grid cols="1 s:2" :x-gap="24" :y-gap="16" responsive="screen">
              <n-form-item-gi label="Banner 标签">
                <n-input
                  v-model:value="formData.bannerTags"
                  placeholder="多个标签使用逗号分隔，留空则不显示"
                  clearable
                />
              </n-form-item-gi>
              <n-form-item-gi label="Banner 图片 URL">
                <div class="banner-url-row">
                  <n-input
                    v-model:value="formData.bannerUrl"
                    placeholder="请输入 Banner 图片地址"
                    clearable
                  />
                  <n-button :loading="uploadingBanner" :disabled="!currentEventId" @click="openBannerFilePicker">
                    <template #icon>
                      <GoUpload />
                    </template>
                    上传
                  </n-button>
                  <input
                    ref="bannerFileInput"
                    class="hidden-file-input"
                    type="file"
                    accept="image/*"
                    @change="handleBannerFileChange"
                  >
                </div>
              </n-form-item-gi>
            </n-grid>

            <n-divider style="margin: 20px 0" />

            <n-grid cols="1 s:2" :x-gap="24" :y-gap="16" responsive="screen">
              <n-form-item-gi label="页面动画">
                <n-switch v-model:value="formData.isAnimation">
                  <template #checked>启用</template>
                  <template #unchecked>禁用</template>
                </n-switch>
              </n-form-item-gi>
              <n-form-item-gi label="顶部导航栏">
                <n-switch v-model:value="formData.isTopNavigationBar">
                  <template #checked>显示</template>
                  <template #unchecked>隐藏</template>
                </n-switch>
              </n-form-item-gi>
            </n-grid>
          </n-card>

          <n-card title="底部信息" :bordered="false" class="settings-card">
            <n-grid cols="1 s:2" :x-gap="24" :y-gap="16" responsive="screen">
              <n-form-item-gi label="ICP备案号">
                <n-input
                  v-model:value="formData.icp"
                  placeholder="例如 京ICP备XXXXXXX号"
                  clearable
                />
              </n-form-item-gi>
              <n-form-item-gi label="技术支持">
                <n-input
                  v-model:value="formData.technicalSupport"
                  placeholder="例如 XX科技有限公司"
                  clearable
                />
              </n-form-item-gi>
            </n-grid>
          </n-card>

          <n-card title="活动状态" :bordered="false" class="settings-card">
            <n-form-item label="是否开放">
              <n-switch v-model:value="formData.isOpen">
                <template #checked>已开放</template>
                <template #unchecked>未开放</template>
              </n-switch>
              <template #feedback>
                <n-text depth="3" style="font-size: 13px">
                  控制活动是否对外可访问。
                </n-text>
              </template>
            </n-form-item>
          </n-card>

          <n-flex justify="end" :size="12">
            <n-button @click="handleReset">重置</n-button>
            <n-button type="primary" size="large" :loading="saving" @click="handleSave">
              保存设置
            </n-button>
          </n-flex>
        </n-space>
      </n-form>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import type { Ref } from 'vue'
import type { Event as ActivityEvent } from '~/types/event'
import { GoUpload } from 'vue-icons-plus/go'

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

const loading = ref(false)
const saving = ref(false)
const uploadingBanner = ref(false)
const bannerFileInput = ref<HTMLInputElement | null>(null)
const currentEventId = computed(() => eventId?.value ?? '')

interface BasicFormData {
  id: number
  url: string
  name: string
  masterTitle: string
  subtitle: string
  startTime: string | null
  endTime: string | null
  bannerTags: string
  bannerUrl: string
  isAnimation: boolean
  isTopNavigationBar: boolean
  icp: string
  technicalSupport: string
  isOpen: boolean
}

interface ActivityPayload {
  id?: number
  url: string
  masterTitle: string
  subtitle: string
  name: string
  startTime: string
  endTime: string
  bannerTags: string
  bannerUrl: string
  isAnimation: boolean
  isTopNavigationBar: boolean
  icp: string
  technicalSupport: string
  isOpen: boolean
}

const createDefaultFormData = (): BasicFormData => ({
  id: 0,
  url: '',
  name: '',
  masterTitle: '',
  subtitle: '',
  startTime: null,
  endTime: null,
  bannerTags: '',
  bannerUrl: '',
  isAnimation: true,
  isTopNavigationBar: true,
  icp: '',
  technicalSupport: '',
  isOpen: false,
})

const formData = reactive<BasicFormData>(createDefaultFormData())

const normalizeText = (value: unknown) => {
  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

const normalizeBoolean = (value: unknown, fallback: boolean) => {
  return typeof value === 'boolean' ? value : fallback
}

const formatDateTimeForPicker = (dateValue: unknown): string | null => {
  const buildDateTime = (
    year: number,
    month: number,
    day: number,
    hours = 0,
    minutes = 0,
  ) => {
    if (
      year < 1000 ||
      month < 1 ||
      month > 12 ||
      hours < 0 ||
      hours > 23 ||
      minutes < 0 ||
      minutes > 59
    ) {
      return null
    }

    const daysInMonth = new Date(year, month, 0).getDate()
    if (day < 1 || day > daysInMonth) {
      return null
    }

    const datePart = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    const timePart = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`

    return `${datePart} ${timePart}`
  }

  const buildFromDate = (date: Date) => {
    if (Number.isNaN(date.getTime())) {
      return null
    }

    return buildDateTime(
      date.getFullYear(),
      date.getMonth() + 1,
      date.getDate(),
      date.getHours(),
      date.getMinutes(),
    )
  }

  if (!dateValue) {
    return null
  }

  if (dateValue instanceof Date) {
    return buildFromDate(dateValue)
  }

  if (typeof dateValue === 'number') {
    return buildFromDate(new Date(dateValue))
  }

  if (typeof dateValue !== 'string') {
    return null
  }

  const value = dateValue.trim()
  if (!value) {
    return null
  }

  const dateTimeMatch = value.match(
    /^(\d{4})[-/:](\d{2})[-/:](\d{2})(?:[ T:](\d{2}):(\d{2}))?/,
  )

  if (dateTimeMatch) {
    const [, year, month, day, hours = '00', minutes = '00'] = dateTimeMatch
    return buildDateTime(Number(year), Number(month), Number(day), Number(hours), Number(minutes))
  }

  return buildFromDate(new Date(value))
}

const assignEventToForm = (event: Partial<ActivityEvent>) => {
  Object.assign(formData, {
    id: Number(event.id) || 0,
    url: normalizeText(event.url),
    name: normalizeText(event.name),
    masterTitle: normalizeText(event.masterTitle),
    subtitle: normalizeText(event.subtitle),
    startTime: formatDateTimeForPicker(event.startTime),
    endTime: formatDateTimeForPicker(event.endTime),
    bannerTags: normalizeText(event.bannerTags),
    bannerUrl: normalizeText(event.bannerUrl),
    isAnimation: normalizeBoolean(event.isAnimation, true),
    isTopNavigationBar: normalizeBoolean(event.isTopNavigationBar, true),
    icp: normalizeText(event.icp),
    technicalSupport: normalizeText(event.technicalSupport),
    isOpen: normalizeBoolean(event.isOpen, false),
  })
}

const resetFormData = () => {
  Object.assign(formData, createDefaultFormData())
}

const getErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) {
    return error.message
  }

  if (typeof error === 'object' && error !== null && 'statusMessage' in error) {
    const statusMessage = (error as { statusMessage?: unknown }).statusMessage
    if (typeof statusMessage === 'string' && statusMessage) {
      return statusMessage
    }
  }

  return fallback
}

function pickUploadUrl(response: unknown): string {
  const visited = new Set<unknown>()
  const urlKeys = ['accessUrl', 'url', 'fileUrl'] as const

  const findUrl = (value: unknown): string => {
    if (!value || typeof value !== 'object' || visited.has(value)) {
      return ''
    }

    visited.add(value)

    if (Array.isArray(value)) {
      for (const item of value) {
        const url = findUrl(item)
        if (url) {
          return url
        }
      }

      return ''
    }

    const record = value as Record<string, unknown>
    for (const key of urlKeys) {
      const candidate = record[key]
      if (typeof candidate === 'string' && candidate.trim()) {
        return candidate.trim()
      }
    }

    for (const nestedValue of Object.values(record)) {
      const url = findUrl(nestedValue)
      if (url) {
        return url
      }
    }

    return ''
  }

  return findUrl(response)
}

function openBannerFilePicker() {
  if (!currentEventId.value) {
    message.warning('请先选择活动')
    return
  }

  bannerFileInput.value?.click()
}

async function handleBannerFileChange(event: globalThis.Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file || !currentEventId.value) {
    input.value = ''
    return
  }

  try {
    uploadingBanner.value = true
    const response = await eventApi.uploadBannerImage(file, currentEventId.value)
    const accessUrl = pickUploadUrl(response)

    if (!accessUrl) {
      message.warning('图片已上传，但接口未返回访问地址')
      return
    }

    formData.bannerUrl = accessUrl
    message.success('Banner 图片上传成功')
  } catch (error) {
    message.error(getErrorMessage(error, 'Banner 图片上传失败'))
  } finally {
    uploadingBanner.value = false
    input.value = ''
  }
}

const buildPayload = (): ActivityPayload => {
  const payload: ActivityPayload = {
    url: formData.url.trim(),
    masterTitle: formData.masterTitle.trim(),
    subtitle: formData.subtitle.trim(),
    name: formData.name.trim(),
    startTime: toISO8601DateTime(formData.startTime) ?? '',
    endTime: toISO8601DateTime(formData.endTime) ?? '',
    bannerTags: formData.bannerTags.trim(),
    bannerUrl: formData.bannerUrl.trim(),
    isAnimation: formData.isAnimation,
    isTopNavigationBar: formData.isTopNavigationBar,
    icp: formData.icp.trim(),
    technicalSupport: formData.technicalSupport.trim(),
    isOpen: formData.isOpen,
  }

  if (formData.id) {
    payload.id = formData.id
  }

  return payload
}

const loadEventData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const event = await eventApi.getEventDetail(eventId.value)
    assignEventToForm(event)
  } catch (error) {
    message.error(getErrorMessage(error, '加载活动数据失败'))
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formData.url.trim() || !formData.name.trim() || !formData.masterTitle.trim()) {
    message.warning('请填写必填项')
    return
  }

  try {
    saving.value = true
    const payload = buildPayload()

    if (formData.id) {
      await eventApi.updateEvent(formData.id, payload)
      message.success('保存成功')
    } else {
      const event = await eventApi.createEvent(payload)
      if (event?.id) {
        assignEventToForm(event)
      }
      message.success('创建成功')
    }
  } catch (error) {
    message.error(getErrorMessage(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  if (formData.id) {
    void loadEventData()
  } else {
    resetFormData()
  }
}

onMounted(() => {
  if (eventId?.value) {
    void loadEventData()
  }
})

watch(() => eventId?.value, (newId) => {
  if (newId) {
    void loadEventData()
  } else {
    resetFormData()
  }
})
</script>

<style scoped>
.basic-settings {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.banner-url-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.hidden-file-input {
  display: none;
}

@media (max-width: 720px) {
  .banner-url-row {
    grid-template-columns: 1fr;
  }
}
</style>
