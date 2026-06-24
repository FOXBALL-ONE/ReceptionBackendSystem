<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">活动管理</h1>
          <p class="page-subtitle">管理和监控所有活动的状态</p>
        </div>
        <n-button type="primary" size="large" class="create-btn" @click="handleCreateEvent">
          <template #icon>
            <n-icon>
              <svg viewBox="0 0 24 24">
                <path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
              </svg>
            </n-icon>
          </template>
          创建活动
        </n-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="4" class="stats-grid">
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <n-icon size="24">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.open }}</div>
              <div class="stat-label">已开放</div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <n-icon size="24">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">未开放</div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon error">
              <n-icon size="24">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.closed }}</div>
              <div class="stat-label">已关闭</div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon primary">
              <n-icon size="24">
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
                </svg>
              </n-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总计</div>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 活动列表 -->
    <n-card :bordered="false" class="table-card">
      <template #header>
        <div class="table-header">
          <h3 class="table-title">活动列表</h3>
          <n-space>
            <n-input
              v-model:value="searchKeyword"
              placeholder="搜索活动"
              clearable
              style="width: 240px"
              @input="handleSearch"
            >
              <template #prefix>
                <n-icon>
                  <svg viewBox="0 0 24 24">
                    <path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                  </svg>
                </n-icon>
              </template>
            </n-input>
          </n-space>
        </div>
      </template>

      <n-spin :show="loading">
        <n-table :bordered="false" :single-line="false" striped class="event-table">
          <thead>
            <tr>
              <th class="col-number">序号</th>
              <th class="col-path">路径</th>
              <th class="col-name">活动名称</th>
              <th class="col-status">访问状态</th>
              <th class="col-time">创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredEvents.length === 0" class="empty-row">
              <td colspan="6" class="empty-cell">
                <n-empty description="暂无活动数据" />
              </td>
            </tr>
            <tr
              v-for="(event, index) in filteredEvents"
              :key="event.id"
              class="table-row"
            >
              <td class="col-number">{{ index + 1 }}</td>
              <td class="col-path">
                <n-text code>{{ event.path }}</n-text>
              </td>
              <td class="col-name">
                <n-text strong>{{ event.name }}</n-text>
              </td>
              <td class="col-status">
                <n-tag
                  :type="EventStatusMap[event.status].type"
                  size="medium"
                  round
                >
                  <template #icon>
                    <n-icon>
                      <svg viewBox="0 0 24 24">
                        <path
                          fill="currentColor"
                          :d="getStatusIconPath(event.status)"
                        />
                      </svg>
                    </n-icon>
                  </template>
                  {{ EventStatusMap[event.status].label }}
                </n-tag>
              </td>
              <td class="col-time">{{ event.createdAt }}</td>
              <td class="col-actions">
                <n-space :size="8" wrap>
                  <n-button
                    size="small"
                    secondary
                    type="primary"
                    @click="handleEdit(event)"
                  >
                    编辑
                  </n-button>
                  <n-button
                    size="small"
                    secondary
                    @click="handleShowQrCode(event)"
                  >
                    二维码
                  </n-button>
                  <n-button
                    size="small"
                    secondary
                    type="warning"
                    :loading="closingEventId === event.id"
                    :disabled="event.status === 'closed'"
                    @click="handleCloseEvent(event)"
                  >
                    关闭
                  </n-button>
                  <n-button
                    size="small"
                    secondary
                    type="error"
                    :loading="deletingEventId === event.id"
                    :disabled="closingEventId === event.id"
                    @click="handleDeleteEvent(event)"
                  >
                    删除
                  </n-button>
                </n-space>
              </td>
            </tr>
          </tbody>
        </n-table>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="createModalVisible"
      preset="card"
      title="创建活动"
      class="create-event-modal"
      style="width: 680px; max-width: calc(100vw - 32px)"
      :bordered="false"
      :mask-closable="!creating"
      :closable="!creating"
    >
      <n-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-placement="top"
        size="medium"
        :show-feedback="true"
        class="create-event-form"
      >
        <n-grid cols="1 s:2" :x-gap="14" :y-gap="4" responsive="screen">
          <n-form-item-gi label="活动名称" path="name">
            <n-input
              v-model:value="createForm.name"
              placeholder="请输入活动名称"
              maxlength="50"
              show-count
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="页面路径" path="url">
            <n-input
              v-model:value="createForm.url"
              placeholder="例如 /event/2026-conference"
              maxlength="100"
              show-count
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="顶部主标题" path="masterTitle">
            <n-input
              v-model:value="createForm.masterTitle"
              placeholder="请输入页面主标题"
              maxlength="50"
              show-count
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="顶部副标题" path="subtitle">
            <n-input
              v-model:value="createForm.subtitle"
              placeholder="请输入副标题"
              maxlength="100"
              show-count
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="开始时间" path="startTime">
            <n-date-picker
              v-model:formatted-value="createForm.startTime"
              type="datetime"
              format="yyyy-MM-dd HH:mm"
              value-format="yyyy-MM-dd HH:mm"
              placeholder="请选择开始时间"
              clearable
              style="width: 100%"
            />
          </n-form-item-gi>
          <n-form-item-gi label="结束时间" path="endTime">
            <n-date-picker
              v-model:formatted-value="createForm.endTime"
              type="datetime"
              format="yyyy-MM-dd HH:mm"
              value-format="yyyy-MM-dd HH:mm"
              placeholder="请选择结束时间"
              clearable
              style="width: 100%"
            />
          </n-form-item-gi>
          <n-form-item-gi label="Banner 标签" path="bannerTags">
            <n-input
              v-model:value="createForm.bannerTags"
              placeholder="多个标签使用逗号分隔"
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="Banner 图片 URL" path="bannerUrl">
            <n-input
              v-model:value="createForm.bannerUrl"
              placeholder="请输入 Banner 图片地址"
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="ICP备案号" path="icp">
            <n-input
              v-model:value="createForm.icp"
              placeholder="例如 京ICP备XXXXXXX号"
              clearable
            />
          </n-form-item-gi>
          <n-form-item-gi label="技术支持" path="technicalSupport">
            <n-input
              v-model:value="createForm.technicalSupport"
              placeholder="例如 XX科技有限公司"
              clearable
            />
          </n-form-item-gi>
        </n-grid>

        <n-divider style="margin: 2px 0 12px" />

        <n-space :size="18" wrap>
          <n-form-item label="是否开放" path="isOpen" :show-feedback="false">
            <n-switch v-model:value="createForm.isOpen">
              <template #checked>已开放</template>
              <template #unchecked>未开放</template>
            </n-switch>
          </n-form-item>
          <n-form-item label="页面动画" path="isAnimation" :show-feedback="false">
            <n-switch v-model:value="createForm.isAnimation">
              <template #checked>启用</template>
              <template #unchecked>禁用</template>
            </n-switch>
          </n-form-item>
          <n-form-item label="顶部导航栏" path="isTopNavigationBar" :show-feedback="false">
            <n-switch v-model:value="createForm.isTopNavigationBar">
              <template #checked>显示</template>
              <template #unchecked>隐藏</template>
            </n-switch>
          </n-form-item>
        </n-space>
      </n-form>

      <template #footer>
        <n-flex justify="end" :size="12">
          <n-button :disabled="creating" @click="createModalVisible = false">
            取消
          </n-button>
          <n-button type="primary" :loading="creating" @click="handleSubmitCreate">
            创建
          </n-button>
        </n-flex>
      </template>
    </n-modal>

    <n-modal
      v-model:show="qrModalVisible"
      preset="card"
      title="活动二维码"
      class="qr-code-modal"
      style="width: 360px; max-width: calc(100vw - 32px)"
      :bordered="false"
    >
      <div class="qr-code-content">
        <div class="qr-event-name">{{ selectedQrEvent?.name }}</div>
        <div class="qr-image-panel">
          <n-spin :show="generatingQrCode">
            <img
              v-if="qrCodeDataUrl"
              class="qr-image"
              :src="qrCodeDataUrl"
              :alt="`${selectedQrEvent?.name ?? '活动'}二维码`"
            >
            <n-empty v-else description="二维码生成失败" />
          </n-spin>
        </div>
        <n-input
          :value="qrCodeTargetUrl"
          readonly
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 3 }"
        />
        <n-flex justify="center" :size="10">
          <n-button secondary @click="copyQrCodeUrl">复制链接</n-button>
          <n-button type="primary" secondary @click="openQrCodeUrl">打开链接</n-button>
        </n-flex>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { NIcon } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { toDataURL } from 'qrcode'
import type { Event, EventDisplayItem, EventStatus } from '~/types/event'
import { EventStatusMap, convertEventToDisplayItem } from '~/types/event'

const eventApi = useEventApi()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const runtimeConfig = useRuntimeConfig()

const loading = ref(false)
const creating = ref(false)
const createModalVisible = ref(false)
const qrModalVisible = ref(false)
const generatingQrCode = ref(false)
const closingEventId = ref<number | null>(null)
const deletingEventId = ref<number | null>(null)
const events = ref<EventDisplayItem[]>([])
const searchKeyword = ref('')
const createFormRef = ref<FormInst | null>(null)
const selectedQrEvent = ref<EventDisplayItem | null>(null)
const qrCodeDataUrl = ref('')

interface CreateEventForm {
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

const createDefaultEventForm = (): CreateEventForm => ({
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

const createForm = reactive<CreateEventForm>(createDefaultEventForm())

const createRules: FormRules = {
  name: {
    required: true,
    message: '请输入活动名称',
    trigger: ['input', 'blur'],
  },
  url: {
    required: true,
    message: '请输入页面路径',
    trigger: ['input', 'blur'],
  },
  masterTitle: {
    required: true,
    message: '请输入顶部主标题',
    trigger: ['input', 'blur'],
  },
}

const stats = computed(() => {
  return {
    open: events.value.filter(e => e.status === 'open').length,
    pending: events.value.filter(e => e.status === 'pending').length,
    closed: events.value.filter(e => e.status === 'closed').length,
    total: events.value.length,
  }
})

const filteredEvents = computed(() => {
  if (!searchKeyword.value.trim()) {
    return events.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return events.value.filter(event =>
    event.name.toLowerCase().includes(keyword) ||
    event.path.toLowerCase().includes(keyword)
  )
})

const normalizeEventList = (rawEvents: unknown): Event[] => {
  if (Array.isArray(rawEvents)) {
    return rawEvents as Event[]
  }

  if (!rawEvents || typeof rawEvents !== 'object') {
    return []
  }

  const record = rawEvents as Record<string, unknown>
  const listKeys = ['list', 'records', 'rows', 'data'] as const

  for (const key of listKeys) {
    const value = record[key]
    if (Array.isArray(value)) {
      return value as Event[]
    }
    if (value && typeof value === 'object') {
      const nestedList = normalizeEventList(value)
      if (nestedList.length) {
        return nestedList
      }
    }
  }

  return []
}

const normalizePath = (value: string) => {
  return value.trim()
}

const getPublicBaseUrl = () => {
  const apiBase = String(runtimeConfig.public.apiBase || '').trim()

  if (!apiBase) {
    return import.meta.client ? window.location.origin : ''
  }

  const currentOrigin = import.meta.client ? window.location.origin : 'http://localhost'
  const parsedUrl = new URL(apiBase, currentOrigin)
  parsedUrl.pathname = parsedUrl.pathname.replace(/\/api\/?$/, '').replace(/\/+$/, '')
  parsedUrl.search = ''
  parsedUrl.hash = ''

  return parsedUrl.toString().replace(/\/$/, '')
}

const buildQrCodeTargetUrl = (path: string) => {
  const baseUrl = getPublicBaseUrl()
  const activityPath = path.trim().replace(/^\/+/, '')

  return `${baseUrl}/s/${activityPath}`
}

const qrCodeTargetUrl = computed(() => {
  if (!selectedQrEvent.value) {
    return ''
  }

  return buildQrCodeTargetUrl(selectedQrEvent.value.path)
})

const buildCreatePayload = (): Partial<Event> => ({
  url: normalizePath(createForm.url),
  name: createForm.name.trim(),
  masterTitle: createForm.masterTitle.trim(),
  subtitle: createForm.subtitle.trim(),
  startTime: toISO8601DateTime(createForm.startTime) ?? '',
  endTime: toISO8601DateTime(createForm.endTime) ?? '',
  bannerTags: createForm.bannerTags.trim(),
  bannerUrl: createForm.bannerUrl.trim(),
  isAnimation: createForm.isAnimation,
  isTopNavigationBar: createForm.isTopNavigationBar,
  icp: createForm.icp.trim(),
  technicalSupport: createForm.technicalSupport.trim(),
  isOpen: createForm.isOpen,
})

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

const fetchEvents = async () => {
  try {
    loading.value = true
    const rawEvents = await eventApi.getEventList()
    events.value = normalizeEventList(rawEvents).map(convertEventToDisplayItem)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '获取活动列表失败'))
    console.error('Failed to fetch events:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
}

const handleCreateEvent = () => {
  Object.assign(createForm, createDefaultEventForm())
  createModalVisible.value = true
  void nextTick(() => {
    createFormRef.value?.restoreValidation()
  })
}

const handleSubmitCreate = async () => {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }

  try {
    creating.value = true
    const createdEvent = await eventApi.createEvent(buildCreatePayload())

    createModalVisible.value = false
    message.success('活动创建成功')
    await fetchEvents()

    if (createdEvent?.id) {
      await router.push(`/event/${createdEvent.id}`)
    }
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '创建活动失败'))
  } finally {
    creating.value = false
  }
}

const handleEdit = (event: EventDisplayItem) => {
  router.push(`/event/${event.id}`)
}

const generateQrCode = async () => {
  if (!qrCodeTargetUrl.value) {
    qrCodeDataUrl.value = ''
    return
  }

  try {
    generatingQrCode.value = true
    qrCodeDataUrl.value = await toDataURL(qrCodeTargetUrl.value, {
      width: 240,
      margin: 1,
      errorCorrectionLevel: 'M',
      color: {
        dark: '#111827',
        light: '#ffffff',
      },
    })
  } catch (error: unknown) {
    qrCodeDataUrl.value = ''
    message.error(getErrorMessage(error, '二维码生成失败'))
  } finally {
    generatingQrCode.value = false
  }
}

const handleShowQrCode = async (event: EventDisplayItem) => {
  selectedQrEvent.value = event
  qrCodeDataUrl.value = ''
  qrModalVisible.value = true
  await generateQrCode()
}

const copyQrCodeUrl = async () => {
  if (!qrCodeTargetUrl.value) {
    return
  }

  try {
    await navigator.clipboard.writeText(qrCodeTargetUrl.value)
    message.success('链接已复制')
  } catch {
    message.warning('复制失败，请手动复制链接')
  }
}

const openQrCodeUrl = () => {
  if (!qrCodeTargetUrl.value || !import.meta.client) {
    return
  }

  window.open(qrCodeTargetUrl.value, '_blank', 'noopener,noreferrer')
}

const handleCloseEvent = async (event: EventDisplayItem) => {
  if (event.status === 'closed') {
    return
  }

  try {
    closingEventId.value = event.id
    await eventApi.updateEventOpenStatus(event.id, false)
    event.status = 'closed'
    message.success(`已关闭活动 "${event.name}"`)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '关闭活动失败'))
  } finally {
    closingEventId.value = null
  }
}

const handleDeleteEvent = (event: EventDisplayItem) => {
  dialog.warning({
    title: '删除活动',
    content: `确定删除活动 "${event.name}" 吗？此操作不可撤销。`,
    positiveText: '删除',
    negativeText: '取消',
    positiveButtonProps: {
      type: 'error',
    },
    onPositiveClick: async () => {
      try {
        deletingEventId.value = event.id
        await eventApi.deleteEvent(event.id)
        events.value = events.value.filter(item => item.id !== event.id)
        message.success(`已删除活动 "${event.name}"`)
      } catch (error: unknown) {
        message.error(getErrorMessage(error, '删除活动失败'))
        return false
      } finally {
        deletingEventId.value = null
      }
    },
  })
}

const getStatusIconPath = (status: EventStatus): string => {
  const icons = {
    open: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
    pending: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z',
    closed: 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
  }
  return icons[status]
}

onMounted(() => {
  fetchEvents()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* 页头样式 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.create-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(24, 160, 88, 0.2);
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 160, 88, 0.3);
}

/* 统计卡片 */
.stats-grid {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  background: white;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.stat-icon.warning {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  color: white;
}

.stat-icon.error {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 表格卡片 */
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background: white;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

/* 表格样式 */
.event-table {
  font-size: 14px;
}

:deep(.event-table th) {
  font-weight: 600;
  background: #fafafa;
  color: #333;
  padding: 16px 12px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.event-table td) {
  padding: 16px 12px;
  vertical-align: middle;
}

:deep(.event-table .table-row) {
  transition: all 0.2s ease;
}

:deep(.event-table .table-row:hover) {
  background: #f8f9fa !important;
}

/* 列宽控制 */
.col-number {
  width: 80px;
  text-align: center;
}

.col-path {
  width: 25%;
}

.col-name {
  width: 25%;
}

.col-status {
  width: 120px;
  text-align: center;
}

.col-time {
  width: 160px;
}

.col-actions {
  width: 300px;
}

/* 响应式优化 */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .create-btn {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: 1fr !important;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

/* 空状态 */
.empty-row {
  background: transparent !important;
}

.empty-cell {
  padding: 48px 24px !important;
  text-align: center;
}

.create-event-modal {
  border-radius: 8px;
}

:deep(.create-event-modal .n-card-header) {
  padding: 18px 22px 10px;
}

:deep(.create-event-modal .n-card__content) {
  max-height: min(62vh, 560px);
  padding: 12px 22px 4px;
  overflow-y: auto;
}

:deep(.create-event-modal .n-card__footer) {
  padding: 12px 22px 18px;
}

.create-event-form :deep(.n-form-item) {
  --n-label-height: 24px;
  --n-feedback-height: 18px;
}

.create-event-form :deep(.n-form-item-label) {
  font-size: 13px;
}

.qr-code-modal {
  border-radius: 8px;
}

:deep(.qr-code-modal .n-card-header) {
  padding: 18px 20px 8px;
}

:deep(.qr-code-modal .n-card__content) {
  padding: 12px 20px 20px;
}

.qr-code-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: stretch;
}

.qr-event-name {
  overflow: hidden;
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qr-image-panel {
  display: flex;
  justify-content: center;
  padding: 14px;
  border: 1px solid #eef0f3;
  border-radius: 8px;
  background: #fafafa;
}

.qr-image {
  display: block;
  width: 240px;
  height: 240px;
}

@media (max-width: 768px) {
  :deep(.create-event-modal .n-card__content) {
    max-height: 68vh;
    padding: 10px 16px 0;
  }

  :deep(.create-event-modal .n-card-header),
  :deep(.create-event-modal .n-card__footer) {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
