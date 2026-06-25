<template>
  <n-space vertical :size="16" class="warm-service-editor">
    <n-card :bordered="false" class="mode-card">
      <div class="mode-switcher">
        <div class="mode-heading">
          <span class="mode-title">{{ activeModeLabel }}</span>
        </div>
        <div class="mode-toggle">
          <span class="mode-option" :class="{ active: !isStaffTipsMode }">参会须知</span>
          <n-switch v-model:value="isStaffTipsMode" aria-label="切换温馨提示模式" />
          <span class="mode-option" :class="{ active: isStaffTipsMode }">工作人员 + 温馨提示</span>
        </div>
      </div>
    </n-card>

    <n-card title="天气情况" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addWeather">
          <template #icon>
            <GoPlus />
          </template>
          添加天气
        </n-button>
      </template>

      <n-empty v-if="weatherItems.length === 0" description="暂无天气">
        <template #extra>
          <n-button type="primary" @click="addWeather">添加天气</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="12">
        <section v-for="(weather, index) in weatherItems" :key="weather.id" class="item-panel">
          <div class="panel-header">
            <div class="panel-title">
              <span class="panel-order">天气 {{ index + 1 }}</span>
              <span class="panel-summary">{{ getWeatherSummary(weather) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="index === 0"
                aria-label="上移天气"
                @click="moveWeather(index, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="index === weatherItems.length - 1"
                aria-label="下移天气"
                @click="moveWeather(index, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除天气" @click="removeWeather(index)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1 s:2 m:4" :x-gap="14" :y-gap="12" responsive="screen">
              <n-form-item-gi label="日期">
                <n-date-picker
                  v-model:formatted-value="weather.date"
                  type="date"
                  format="yyyy:MM:dd"
                  value-format="yyyy:MM:dd"
                  placeholder="选择日期"
                  clearable
                />
              </n-form-item-gi>
              <n-form-item-gi label="城市">
                <n-input v-model:value="weather.city" placeholder="城市名称" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="温度">
                <n-input v-model:value="weather.temperature" placeholder="如 20-28℃" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="天气">
                <n-input v-model:value="weather.condition" placeholder="天气情况" clearable />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </section>
      </n-space>
    </n-card>

    <ConferenceNoticeMode
      v-if="!isStaffTipsMode"
      key="conference-notice-mode"
      :conference-notice="conferenceNotice"
    />
    <StaffTipsMode
      v-else
      key="staff-tips-mode"
      :staff-groups="staffGroups"
      :warm-tips="warmTips"
      :add-staff-group="addStaffGroup"
      :remove-staff-group="removeStaffGroup"
      :move-staff-group="moveStaffGroup"
      :add-staff-member="addStaffMember"
      :remove-staff-member="removeStaffMember"
      :add-warm-tip="addWarmTip"
      :remove-warm-tip="removeWarmTip"
      :move-warm-tip="moveWarmTip"
    />

    <n-flex justify="end" class="footer-actions">
      <n-button type="primary" :loading="saving" @click="saveWarmService">保存</n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash } from "vue-icons-plus/go";
import ConferenceNoticeMode from "~/composables/event/warm-service/ConferenceNoticeMode.vue";
import StaffTipsMode from "~/composables/event/warm-service/StaffTipsMode.vue";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface WeatherItem {
  id: string;
  date: string | null;
  city: string;
  temperature: string;
  condition: string;
}

interface ConferenceNotice {
  title: string;
  content: string;
}

interface StaffMember {
  id: string;
  name: string;
  role: string;
  phone: string;
}

interface StaffGroup {
  id: string;
  name: string;
  members: StaffMember[];
}

interface WarmTip {
  id: string;
  title: string;
  content: string;
}

interface PromptServicePayload {
  id?: number | string | null;
  activityId?: number | string;
  staffList?: Array<{
    name?: string | null;
    colorTag?: string | null;
    groupList?: Array<{
      name?: string | null;
      duty?: string | null;
      phone?: string | null;
    }>;
  }>;
  noteList?: Array<{
    title?: string | null;
    content?: string | null;
    colorTag?: string | null;
  }>;
  weatherList?: Array<{
    time?: string | null;
    city?: string | null;
    temperature?: string | null;
    weatherDescriptor?: string | null;
  }>;
  attendanceInstructionsMode?: boolean | null;
  attendanceInstructionsTitle?: string | null;
  attendanceInstructionsContent?: string | null;
}

const idCounters = {
  group: 1,
  member: 1,
  tip: 1,
  weather: 1,
};

const isStaffTipsMode = ref(false);
const conferenceNotice = ref<ConferenceNotice>({
  title: "",
  content: "",
});
const weatherItems = ref<WeatherItem[]>([createWeatherItem()]);
const staffGroups = ref<StaffGroup[]>([createStaffGroup("会务组")]);
const warmTips = ref<WarmTip[]>([createWarmTip()]);
const loading = ref(false);
const saving = ref(false);
const promptServiceId = ref<number | string | null>(null);

const activeModeLabel = computed(() => (isStaffTipsMode.value ? "工作人员 + 温馨提示" : "参会须知"));

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createWeatherItem(values: Partial<Omit<WeatherItem, "id">> = {}): WeatherItem {
  return {
    id: nextId("weather"),
    date: values.date ?? null,
    city: values.city ?? "",
    temperature: values.temperature ?? "",
    condition: values.condition ?? "",
  };
}

function createStaffMember(values: Partial<Omit<StaffMember, "id">> = {}): StaffMember {
  return {
    id: nextId("member"),
    name: values.name ?? "",
    role: values.role ?? "",
    phone: values.phone ?? "",
  };
}

function createStaffGroup(name = ""): StaffGroup {
  return {
    id: nextId("group"),
    name,
    members: [createStaffMember()],
  };
}

function createWarmTip(values: Partial<Omit<WarmTip, "id">> = {}): WarmTip {
  return {
    id: nextId("tip"),
    title: values.title ?? "",
    content: values.content ?? "",
  };
}

function moveItem<T>(items: T[], index: number, offset: -1 | 1) {
  const targetIndex = index + offset;
  if (targetIndex < 0 || targetIndex >= items.length) {
    return;
  }

  const [item] = items.splice(index, 1);
  items.splice(targetIndex, 0, item);
}

function addWeather() {
  weatherItems.value.push(createWeatherItem());
}

function removeWeather(index: number) {
  weatherItems.value.splice(index, 1);
}

function moveWeather(index: number, offset: -1 | 1) {
  moveItem(weatherItems.value, index, offset);
}

function addStaffGroup() {
  staffGroups.value.push(createStaffGroup(`分组 ${staffGroups.value.length + 1}`));
}

function removeStaffGroup(index: number) {
  staffGroups.value.splice(index, 1);
}

function moveStaffGroup(index: number, offset: -1 | 1) {
  moveItem(staffGroups.value, index, offset);
}

function addStaffMember(group: StaffGroup) {
  group.members.push(createStaffMember());
}

function removeStaffMember(group: StaffGroup, index: number) {
  group.members.splice(index, 1);
}

function addWarmTip() {
  warmTips.value.push(createWarmTip());
}

function removeWarmTip(index: number) {
  warmTips.value.splice(index, 1);
}

function moveWarmTip(index: number, offset: -1 | 1) {
  moveItem(warmTips.value, index, offset);
}

function getWeatherSummary(weather: WeatherItem) {
  const summaryParts = [weather.date, weather.city, weather.temperature, weather.condition].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写天气信息";
}

function resetWarmServiceState() {
  promptServiceId.value = null
  isStaffTipsMode.value = false
  conferenceNotice.value = {
    title: "",
    content: "",
  }
  weatherItems.value = [createWeatherItem()]
  staffGroups.value = [createStaffGroup("会务组")]
  warmTips.value = [createWarmTip()]
}

function formatWeatherDate(dateValue: unknown): string | null {
  if (!dateValue) return null

  const buildWeatherDate = (year: number, month: number, day: number) => {
    if (year < 1000 || month < 1 || month > 12) {
      return null
    }

    const daysInMonth = new Date(year, month, 0).getDate()
    if (day < 1 || day > daysInMonth) {
      return null
    }

    return `${year}:${String(month).padStart(2, '0')}:${String(day).padStart(2, '0')}`
  }

  const buildFromDate = (date: Date) => {
    if (Number.isNaN(date.getTime())) {
      return null
    }

    return buildWeatherDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
  }

  if (dateValue instanceof Date) {
    return buildFromDate(dateValue)
  }

  if (typeof dateValue === 'number') {
    return buildFromDate(new Date(dateValue))
  }

  if (typeof dateValue === 'string') {
    const value = dateValue.trim()
    if (!value) return null

    const dateMatch = value.match(/^(\d{4})[-/:](\d{2})[-/:](\d{2})/)

    if (dateMatch) {
      const [, year, month, day] = dateMatch
      return buildWeatherDate(Number(year), Number(month), Number(day))
    }

    return buildFromDate(new Date(value))
  }

  return null
}

function pickPromptService(response: PromptServicePayload | PromptServicePayload[] | null | undefined) {
  if (Array.isArray(response)) {
    return response[0] ?? null
  }

  return response ?? null
}

const loadWarmServiceData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const data = pickPromptService(await eventApi.getPromptServiceByActivity(eventId.value))

    if (data) {
      promptServiceId.value = data.id ?? null

      if (data.weatherList && Array.isArray(data.weatherList) && data.weatherList.length > 0) {
        weatherItems.value = data.weatherList.map((weather) => createWeatherItem({
          date: formatWeatherDate(weather.time),
          city: weather.city || '',
          temperature: weather.temperature || '',
          condition: weather.weatherDescriptor || '',
        }))
      }

      if (!data.attendanceInstructionsMode) {
        isStaffTipsMode.value = true
        if (data.staffList && Array.isArray(data.staffList)) {
          staffGroups.value = data.staffList.map((group) => ({
            id: nextId('group'),
            name: group.name || '',
            members: (group.groupList || []).map((member) => ({
              id: nextId('member'),
              name: member.name || '',
              role: member.duty || '',
              phone: member.phone || '',
            }))
          }))
        }
        if (data.noteList && Array.isArray(data.noteList)) {
          warmTips.value = data.noteList.map((tip) => createWarmTip({
            title: tip.title || '',
            content: tip.content || '',
          }))
        }
      } else {
        isStaffTipsMode.value = false
        conferenceNotice.value = {
          title: data.attendanceInstructionsTitle || '',
          content: data.attendanceInstructionsContent || '',
        }
      }
    } else {
      resetWarmServiceState()
    }
  } catch (error: any) {
    // 如果没有数据或加载失败，使用默认值
    resetWarmServiceState()
    console.warn('加载温馨服务数据失败:', error.message)
  } finally {
    loading.value = false
  }
}

function buildSavePayload() {
  const weatherList = weatherItems.value.map((weather) => ({
    time: weather.date,
    city: weather.city.trim(),
    temperature: weather.temperature.trim(),
    weatherDescriptor: weather.condition.trim(),
  }));

  if (!isStaffTipsMode.value) {
    return {
      ...(promptServiceId.value ? { id: promptServiceId.value } : {}),
      weatherList,
      staffList: [],
      noteList: [],
      attendanceInstructionsMode: true,
      attendanceInstructionsTitle: conferenceNotice.value.title.trim(),
      attendanceInstructionsContent: conferenceNotice.value.content.trim(),
    };
  }

  return {
    ...(promptServiceId.value ? { id: promptServiceId.value } : {}),
    weatherList,
    staffList: staffGroups.value.map((group) => ({
      name: group.name.trim(),
      colorTag: '',
      groupList: group.members
        .map((member) => ({
          name: member.name.trim(),
          duty: member.role.trim(),
          phone: member.phone.trim(),
        }))
        .filter((member) => member.name || member.duty || member.phone),
    })),
    noteList: warmTips.value.map((tip) => ({
      title: tip.title.trim(),
      content: tip.content.trim(),
      colorTag: '',
    })),
    attendanceInstructionsMode: false,
    attendanceInstructionsTitle: '',
    attendanceInstructionsContent: '',
  };
}

async function saveWarmService() {
  if (!eventId?.value) {
    message.warning("请先选择活动");
    return;
  }

  if (!isStaffTipsMode.value && !conferenceNotice.value.title.trim() && !conferenceNotice.value.content.trim()) {
    message.warning("请先填写参会须知");
    return;
  }

  if (isStaffTipsMode.value && staffGroups.value.length === 0 && warmTips.value.length === 0) {
    message.warning("请先填写工作人员或温馨提示");
    return;
  }

  try {
    saving.value = true;
    const payload = buildSavePayload();

    // 调用 prompt-services 接口
    await eventApi.savePromptService(eventId.value, payload);

    message.success(`${activeModeLabel.value}已保存`);
  } catch (error: any) {
    message.error(error.message || "保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (eventId?.value) {
    loadWarmServiceData()
  }
})

watch(() => eventId?.value, (newId) => {
  if (newId) {
    loadWarmServiceData()
  }
})
</script>

<style scoped>
.warm-service-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.mode-switcher {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.mode-heading {
  min-width: 0;
}

.mode-title {
  color: #101828;
  font-size: 16px;
  font-weight: 650;
}

.mode-toggle {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.mode-option {
  color: #667085;
  font-size: 14px;
  font-weight: 600;
}

.mode-option.active {
  color: #175cd3;
}

.item-panel {
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.panel-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.panel-summary {
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
  .mode-switcher,
  .panel-header,
  .panel-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .mode-toggle {
    justify-content: flex-start;
  }

  .panel-title,
  .panel-summary {
    width: 100%;
  }

  .panel-summary {
    white-space: normal;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
