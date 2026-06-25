<template>
  <n-space vertical :size="16" class="xls-editor">
    <n-card title="Excel 模板与导入" :bordered="false">
      <n-space vertical :size="10">
        <n-text depth="2">
          通过 Excel 批量录入活动数据：先下载导入模板，按各 sheet 表头填写后上传即可。
          也可导出当前活动作为备份或参考样例。
        </n-text>
        <n-flex :size="10" wrap>
          <n-button type="primary" :loading="downloading.template" @click="downloadTemplate">
            下载导入模板
          </n-button>
          <n-button
            :loading="downloading.exporting"
            :disabled="!eventId"
            @click="downloadExport"
          >
            导出当前活动
          </n-button>
        </n-flex>
      </n-space>
    </n-card>

    <n-card title="导入数据" :bordered="false">
      <n-space vertical :size="14">
        <n-form label-placement="left" :show-feedback="false">
          <n-form-item label="导入方式">
            <n-radio-group v-model:value="importMode" name="excel-import-mode">
              <n-radio value="new">导入为新活动</n-radio>
              <n-radio value="overwrite" :disabled="!eventId">覆盖当前活动</n-radio>
            </n-radio-group>
          </n-form-item>
        </n-form>

        <n-alert v-if="importMode === 'overwrite'" type="warning" :show-icon="true">
          覆盖会删除当前活动的全部数据并按 Excel 重建（活动 ID 会变更，已上传的横幅 / 图片等配置不会自动保留）。请确认或先导出备份。
        </n-alert>

        <n-upload
          :default-upload="false"
          :max="1"
          accept=".xlsx,.xls"
          @change="onUploadChange"
        >
          <n-upload-dragger>
            <div class="upload-dropzone">
              <span class="upload-icon">📥</span>
              <p class="upload-title">点击或拖拽 Excel 文件到此处</p>
              <p class="upload-sub">仅支持 .xlsx / .xls 格式，需使用上方模板的表头结构</p>
            </div>
          </n-upload-dragger>
        </n-upload>

        <n-flex :size="10" align="center">
          <n-button
            type="primary"
            :loading="importing"
            :disabled="!selectedFile"
            @click="doImport"
          >
            开始导入
          </n-button>
          <n-text v-if="selectedFile" depth="3" style="font-size: 13px">
            已选择：{{ selectedFile.name }}
          </n-text>
        </n-flex>
      </n-space>
    </n-card>

    <n-card v-if="result" title="导入结果" :bordered="false">
      <n-space vertical :size="12">
        <n-space :size="8" wrap>
          <n-tag v-for="item in resultTags" :key="item.label" type="success" round>
            {{ item.label }}：{{ item.value }}
          </n-tag>
        </n-space>
        <n-flex :size="10">
          <n-button
            v-if="result.activityId"
            type="primary"
            @click="openImportedActivity"
          >
            打开活动
          </n-button>
          <n-button quaternary @click="resetImport">清空结果</n-button>
        </n-flex>
      </n-space>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import type { UploadFileInfo } from 'naive-ui'

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()
const dialog = useDialog()

interface ExcelImportResult {
  activityId: number | string | null
  url: string | null
  personnelCount: number
  carCount: number
  mealCount: number
  lodgingCount: number
  lodgingStaffGroupCount: number
  scheduleCount: number
  inspectionPointCount: number
  attendanceGuidelineCount: number
  overviewCount: number
}

type ImportMode = 'new' | 'overwrite'

const importMode = ref<ImportMode>('new')
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const result = ref<ExcelImportResult | null>(null)
const downloading = reactive({ template: false, exporting: false })

const resultTags = computed(() => {
  const r = result.value
  if (!r) return []
  return [
    { label: '人员', value: r.personnelCount },
    { label: '乘车', value: r.carCount },
    { label: '用餐', value: r.mealCount },
    { label: '住宿', value: r.lodgingCount },
    { label: '工作人员组', value: r.lodgingStaffGroupCount },
    { label: '日程', value: r.scheduleCount },
    { label: '考察点', value: r.inspectionPointCount },
    { label: '参会须知', value: r.attendanceGuidelineCount },
    { label: '市县概况', value: r.overviewCount },
  ]
})

function onUploadChange(options: { file: UploadFileInfo }) {
  const file = options.file?.file ?? null
  selectedFile.value = file
}

function isExcelFile(file: File) {
  return /\.(xlsx|xls)$/i.test(file.name)
}

function triggerDownload(url: string) {
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.style.display = 'none'
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
}

async function downloadTemplate() {
  try {
    downloading.template = true
    triggerDownload(eventApi.getExcelTemplateUrl())
  } catch (error: any) {
    message.error(error?.message || '模板下载失败')
  } finally {
    downloading.template = false
  }
}

async function downloadExport() {
  if (!eventId?.value) {
    message.warning('当前没有可导出的活动')
    return
  }
  try {
    downloading.exporting = true
    triggerDownload(eventApi.getExcelExportUrl(eventId.value))
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  } finally {
    downloading.exporting = false
  }
}

function confirmOverwrite(): Promise<boolean> {
  return new Promise((resolve) => {
    dialog.warning({
      title: '确认覆盖当前活动',
      content: '将删除当前活动的全部数据并按 Excel 重建，活动 ID 会变更，且不会保留已上传的横幅 / 图片等配置。建议先「导出当前活动」备份。是否继续？',
      positiveText: '覆盖导入',
      negativeText: '取消',
      onPositiveClick: () => resolve(true),
      onNegativeClick: () => resolve(false),
      onMaskClick: () => resolve(false),
    })
  })
}

async function doImport() {
  const file = selectedFile.value
  if (!file) {
    message.warning('请先选择 Excel 文件')
    return
  }
  if (!isExcelFile(file)) {
    message.warning('仅支持 .xlsx / .xls 格式')
    return
  }

  const overwrite = importMode.value === 'overwrite'
  if (overwrite) {
    if (!eventId?.value) {
      message.warning('当前没有可覆盖的活动')
      return
    }
    const ok = await confirmOverwrite()
    if (!ok) return
  }

  try {
    importing.value = true

    const data = await eventApi.importExcel(file, {
      activityId: overwrite && eventId?.value ? eventId.value : undefined,
    }) as ExcelImportResult

    result.value = data
    message.success(
      overwrite
        ? `已覆盖当前活动数据（新活动 ID：${data.activityId ?? '-'}）`
        : `已创建新活动（ID：${data.activityId ?? '-'}）`,
    )

    if (overwrite && data.activityId) {
      await navigateTo(`/event/${data.activityId}`)
    }
  } catch (error: any) {
    message.error(error?.message || '导入失败，请检查文件格式是否与模板一致')
  } finally {
    importing.value = false
  }
}

function openImportedActivity() {
  const id = result.value?.activityId
  if (id !== null && id !== undefined) {
    navigateTo(`/event/${id}`)
  }
}

function resetImport() {
  result.value = null
  selectedFile.value = null
}
</script>

<style scoped>
.xls-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.upload-dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 22px 12px;
}

.upload-icon {
  font-size: 30px;
  line-height: 1;
}

.upload-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #101828;
}

.upload-sub {
  margin: 0;
  font-size: 13px;
  color: #667085;
}
</style>
