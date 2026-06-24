<template>
  <n-space vertical :size="16" class="schedule-editor">
    <n-card title="日程安排" :bordered="false">
      <template #header-extra>
        <n-space>
          <n-button @click="addDay">
            <template #icon>
              <GoPlus />
            </template>
            添加日期
          </n-button>
          <n-button type="primary" @click="addGroup">
            <template #icon>
              <GoPlus />
            </template>
            添加考察组
          </n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">
            保存日程
          </n-button>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-empty v-if="scheduleDays.length === 0 && groups.length === 0" description="暂无日程">
          <template #extra>
            <n-space>
              <n-button type="primary" @click="addDay">添加日期</n-button>
              <n-button type="primary" @click="addGroup">添加考察组</n-button>
            </n-space>
          </template>
        </n-empty>

        <n-space v-else vertical :size="16">
          <n-card
            v-for="(group, groupIndex) in groups"
            :key="group.id"
            class="group-card"
            :bordered="true"
          >
            <template #header>
              <div class="group-heading">
                <span class="group-order">考察组 {{ groupIndex + 1 }}</span>
                <n-input v-model:value="group.name" placeholder="请输入组名" clearable />
              </div>
            </template>

            <template #header-extra>
              <n-flex :size="6" align="center">
                <n-button
                  circle
                  quaternary
                  :disabled="groupIndex === 0"
                  aria-label="上移考察组"
                  @click="moveGroup(groupIndex, -1)"
                >
                  <template #icon>
                    <GoArrowUp />
                  </template>
                </n-button>
                <n-button
                  circle
                  quaternary
                  :disabled="groupIndex === groups.length - 1"
                  aria-label="下移考察组"
                  @click="moveGroup(groupIndex, 1)"
                >
                  <template #icon>
                    <GoArrowDown />
                  </template>
                </n-button>
                <n-button
                  circle
                  quaternary
                  type="error"
                  aria-label="删除考察组"
                  @click="removeGroup(groupIndex)"
                >
                  <template #icon>
                    <GoTrash />
                  </template>
                </n-button>
              </n-flex>
            </template>

            <n-empty v-if="scheduleDays.length === 0" description="请先添加日期">
              <template #extra>
                <n-button secondary type="primary" @click="addDay">添加日期</n-button>
              </template>
            </n-empty>

            <n-space v-else vertical :size="16">
              <n-card
                v-for="itinerary in group.itineraries"
                :key="itinerary.id"
                class="day-card"
                :bordered="true"
                size="small"
              >
                <template #header>
                  <div class="day-heading">
                    <span class="day-order">{{ dayLabelOf(itinerary) }}</span>
                    <n-input
                      :value="dateTagOf(itinerary)"
                      class="date-tag-input"
                      placeholder="日期标签，留空不显示"
                      clearable
                      @update:value="(value: string) => setDateTagOf(itinerary, value)"
                    />
                  </div>
                </template>

                <n-space vertical :size="14">
                  <n-form label-placement="top" :show-feedback="false">
                    <n-grid cols="1 m:2" :x-gap="18" :y-gap="14" responsive="screen">
                      <n-form-item-gi label="路线图">
                        <div class="url-control">
                          <n-input v-model:value="itinerary.routeMapUrl" placeholder="路线图 URL" clearable />
                          <label class="upload-control">
                            <GoUpload />
                            {{ uploading ? "上传中" : "上传" }}
                            <input
                              type="file"
                              accept="image/*,.pdf"
                              :disabled="uploading"
                              @change="handleLocalFileChange($event, itinerary, 'routeMapUrl')"
                            >
                          </label>
                        </div>
                      </n-form-item-gi>
                      <n-form-item-gi label="日程表">
                        <div class="url-control">
                          <n-input
                            v-model:value="itinerary.scheduleUrl"
                            placeholder="会议日程表 URL，可代替下方逐行表单"
                            clearable
                          />
                          <label class="upload-control">
                            <GoUpload />
                            {{ uploading ? "上传中" : "上传" }}
                            <input
                              type="file"
                              accept="image/*,.pdf,.xls,.xlsx"
                              :disabled="uploading"
                              @change="handleLocalFileChange($event, itinerary, 'scheduleUrl')"
                            >
                          </label>
                        </div>
                      </n-form-item-gi>
                    </n-grid>
                  </n-form>

                  <n-card title="路线节点" class="sub-card" size="small">
                    <template #header-extra>
                      <n-button size="small" secondary type="primary" @click="addRouteNode(itinerary)">
                        <template #icon>
                          <GoPlus />
                        </template>
                        添加节点
                      </n-button>
                    </template>

                    <n-empty v-if="itinerary.nodes.length === 0" description="暂无路线节点" />

                    <n-space v-else vertical :size="10">
                      <div v-for="(node, nodeIndex) in itinerary.nodes" :key="node.id" class="node-row">
                        <span class="row-index">{{ nodeIndex + 1 }}</span>
                        <n-input v-model:value="node.name" placeholder="请输入路线节点" clearable />
                        <div class="row-actions">
                          <n-button
                            circle
                            quaternary
                            :disabled="nodeIndex === 0"
                            aria-label="上移路线节点"
                            @click="moveRouteNode(itinerary, nodeIndex, -1)"
                          >
                            <template #icon>
                              <GoArrowUp />
                            </template>
                          </n-button>
                          <n-button
                            circle
                            quaternary
                            :disabled="nodeIndex === itinerary.nodes.length - 1"
                            aria-label="下移路线节点"
                            @click="moveRouteNode(itinerary, nodeIndex, 1)"
                          >
                            <template #icon>
                              <GoArrowDown />
                            </template>
                          </n-button>
                          <n-button
                            circle
                            quaternary
                            type="error"
                            aria-label="删除路线节点"
                            @click="removeRouteNode(itinerary, nodeIndex)"
                          >
                            <template #icon>
                              <GoTrash />
                            </template>
                          </n-button>
                        </div>
                      </div>
                    </n-space>
                  </n-card>

                  <n-card title="该组会议及活动安排" class="sub-card" size="small">
                    <template #header-extra>
                      <n-button size="small" secondary type="primary" @click="addActivity(itinerary)">
                        <template #icon>
                          <GoPlus />
                        </template>
                        添加安排
                      </n-button>
                    </template>

                    <n-empty v-if="itinerary.activities.length === 0" description="暂无活动安排" />

                    <n-space v-else vertical :size="10">
                      <div
                        v-for="(activity, activityIndex) in itinerary.activities"
                        :key="activity.id"
                        class="activity-row"
                      >
                        <n-time-picker
                          v-model:formatted-value="activity.startTime"
                          format="HH:mm"
                          value-format="HH:mm"
                          placeholder="开始"
                          clearable
                        />
                        <span class="time-separator">-</span>
                        <n-time-picker
                          v-model:formatted-value="activity.endTime"
                          format="HH:mm"
                          value-format="HH:mm"
                          placeholder="结束"
                          clearable
                        />
                        <n-input v-model:value="activity.content" placeholder="活动内容" clearable />
                        <n-input v-model:value="activity.location" placeholder="地点" clearable />
                        <div class="row-actions">
                          <n-button
                            circle
                            quaternary
                            :disabled="activityIndex === 0"
                            aria-label="上移活动安排"
                            @click="moveActivity(itinerary, activityIndex, -1)"
                          >
                            <template #icon>
                              <GoArrowUp />
                            </template>
                          </n-button>
                          <n-button
                            circle
                            quaternary
                            :disabled="activityIndex === itinerary.activities.length - 1"
                            aria-label="下移活动安排"
                            @click="moveActivity(itinerary, activityIndex, 1)"
                          >
                            <template #icon>
                              <GoArrowDown />
                            </template>
                          </n-button>
                          <n-button
                            circle
                            quaternary
                            type="error"
                            aria-label="删除活动安排"
                            @click="removeActivity(itinerary, activityIndex)"
                          >
                            <template #icon>
                              <GoTrash />
                            </template>
                          </n-button>
                        </div>
                      </div>
                    </n-space>
                  </n-card>
                </n-space>
              </n-card>
            </n-space>
          </n-card>
        </n-space>
      </n-spin>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash, GoUpload } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

type UploadUrlField = "routeMapUrl" | "scheduleUrl";

interface RouteNode {
  id: string;
  name: string;
}

interface ScheduleActivity {
  id: string;
  startTime: string | null;
  endTime: string | null;
  content: string;
  location: string;
}

/** 单个考察组在某一日的行程。 */
interface Itinerary {
  id: string;
  backendId: number | null;
  scheduleId: number | null;
  dayId: string;
  routeMapUrl: string;
  scheduleUrl: string;
  nodes: RouteNode[];
  activities: ScheduleActivity[];
}

interface InspectionGroup {
  id: string;
  backendId: number | null;
  name: string;
  itineraries: Itinerary[];
}

interface ScheduleDay {
  id: string;
  backendId: number | null;
  dateTag: string;
}

const idCounters = {
  day: 1,
  group: 1,
  itinerary: 1,
  node: 1,
  activity: 1,
};

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createRouteNode(name = ""): RouteNode {
  return {
    id: nextId("node"),
    name,
  };
}

function createActivity(values: Partial<Omit<ScheduleActivity, "id">> = {}): ScheduleActivity {
  return {
    id: nextId("activity"),
    startTime: values.startTime ?? "09:00",
    endTime: values.endTime ?? "10:00",
    content: values.content ?? "",
    location: values.location ?? "",
  };
}

function createItinerary(day: ScheduleDay, values: Partial<Omit<Itinerary, "id" | "nodes" | "activities">> & {
  nodes?: RouteNode[];
  activities?: ScheduleActivity[];
} = {}): Itinerary {
  return {
    id: nextId("itinerary"),
    backendId: values.backendId ?? null,
    scheduleId: day.backendId,
    dayId: day.id,
    routeMapUrl: values.routeMapUrl ?? "",
    scheduleUrl: values.scheduleUrl ?? "",
    nodes: values.nodes ?? [createRouteNode()],
    activities: values.activities ?? [createActivity()],
  };
}

function createDay(dateTag = "", backendId: number | null = null): ScheduleDay {
  return {
    id: nextId("day"),
    backendId,
    dateTag,
  };
}

function createGroup(values: Partial<Omit<InspectionGroup, "id" | "itineraries">> & {
  itineraries?: Itinerary[];
} = {}): InspectionGroup {
  return {
    id: nextId("group"),
    backendId: values.backendId ?? null,
    name: values.name ?? "",
    itineraries: values.itineraries ?? scheduleDays.value.map((day) => createItinerary(day)),
  };
}

const scheduleDays = ref<ScheduleDay[]>([]);
const groups = ref<InspectionGroup[]>([]);

function toBackendId(value: unknown): number | null {
  const numericValue = Number(value);

  return Number.isFinite(numericValue) && numericValue > 0 ? numericValue : null;
}

function extractTime(value: unknown): string | null {
  if (!value) {
    return null;
  }

  if (typeof value === "string") {
    const timeMatch = value.match(/(?:^|T|\s)(\d{1,2}):(\d{2})/);
    if (timeMatch) {
      const [, hours, minutes] = timeMatch;

      return `${String(Number(hours)).padStart(2, "0")}:${minutes}`;
    }
  }

  const date = new Date(value as string | number | Date);
  if (Number.isNaN(date.getTime())) {
    return null;
  }

  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${hours}:${minutes}`;
}

function combineDateTime(dateTag: string, time: string | null): string {
  if (!time) {
    return "";
  }

  let baseDate = new Date();
  const dateMatch = dateTag.match(/(\d{4})[年\-/](\d{1,2})[月\-/](\d{1,2})/);
  if (dateMatch) {
    const [, year, month, day] = dateMatch;
    baseDate = new Date(Number(year), Number(month) - 1, Number(day));
  }

  const timeMatch = time.match(/^(\d{1,2}):(\d{2})/);
  if (!timeMatch) {
    return "";
  }

  const [, hours, minutes] = timeMatch;
  baseDate.setHours(Number(hours), Number(minutes), 0, 0);

  return Number.isNaN(baseDate.getTime()) ? "" : baseDate.toISOString();
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

function dayById(dayId: string): ScheduleDay | undefined {
  return scheduleDays.value.find((day) => day.id === dayId);
}

function dayLabelOf(itinerary: Itinerary): string {
  const index = scheduleDays.value.findIndex((day) => day.id === itinerary.dayId);
  return index >= 0 ? `第 ${index + 1} 天` : "未排期";
}

function dateTagOf(itinerary: Itinerary): string {
  return dayById(itinerary.dayId)?.dateTag ?? "";
}

function setDateTagOf(itinerary: Itinerary, value: string) {
  const day = dayById(itinerary.dayId);
  if (day) {
    day.dateTag = value;
  }
}

const loadScheduleData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const [schedules, inspectionTeams] = await Promise.all([
      eventApi.getSchedulesByActivity(eventId.value),
      eventApi.getInspectionTeamItems(eventId.value),
    ])

    // 天注册表
    const rawDays = Array.isArray(schedules) ? schedules : []
    scheduleDays.value = rawDays.length > 0
      ? rawDays.map((schedule: any) => createDay(schedule.scheduleTags || '', toBackendId(schedule.id)))
      : [createDay("第一天")]

    // 考察组（身份 + 各天行程），按天补齐缺失行程
    const rawTeams = Array.isArray(inspectionTeams) ? inspectionTeams : []
    groups.value = rawTeams.length > 0
      ? rawTeams.map((team: any) => normalizeGroup(team))
      : [createGroup()]

    ensureItinerariesForAllGroups()
  } catch (error) {
    message.error(getErrorMessage(error, '加载日程数据失败'))
    scheduleDays.value = [createDay("第一天")]
    groups.value = [createGroup()]
  } finally {
    loading.value = false
  }
}

function normalizeGroup(team: any): InspectionGroup {
  const group = createGroup({
    backendId: toBackendId(team.id),
    name: team.name || '',
  })

  const rawItineraries = Array.isArray(team.itineraries) ? team.itineraries : []
  group.itineraries = scheduleDays.value.map((day) => {
    const matched = rawItineraries.find((item: any) => toBackendId(item.scheduleId) === day.backendId)
    if (!matched) return createItinerary(day)

    const nodes = Array.isArray(matched.routeNode) && matched.routeNode.length > 0
      ? matched.routeNode.map((nodeName: string) => createRouteNode(nodeName))
      : [createRouteNode()]
    const activities = Array.isArray(matched.eventArrangements) && matched.eventArrangements.length > 0
      ? matched.eventArrangements.map((arrangement: any) => createActivity({
        startTime: extractTime(arrangement.startTime),
        endTime: extractTime(arrangement.endTime),
        content: arrangement.content || '',
        location: arrangement.location || '',
      }))
      : [createActivity()]

    return createItinerary(day, {
      backendId: toBackendId(matched.id),
      routeMapUrl: matched.routeUrl || '',
      scheduleUrl: matched.scheduleUrl || '',
      nodes,
      activities,
    })
  })

  return group
}

/** 为每个考察组补齐当前天列表中存在、但该组尚缺的行程；移除已不存在天的行程。 */
function ensureItinerariesForAllGroups() {
  groups.value.forEach((group) => {
    const present = new Set(group.itineraries.map((it) => it.dayId))
    scheduleDays.value.forEach((day) => {
      if (!present.has(day.id)) {
        group.itineraries.push(createItinerary(day))
      }
    })
    group.itineraries = group.itineraries.filter((it) =>
      scheduleDays.value.some((day) => day.id === it.dayId),
    )
    // 按天顺序对齐展示
    group.itineraries.sort((a, b) => dayIndexOf(a.dayId) - dayIndexOf(b.dayId))
  })
}

function dayIndexOf(dayId: string): number {
  const index = scheduleDays.value.findIndex((day) => day.id === dayId)
  return index >= 0 ? index : Number.MAX_SAFE_INTEGER
}

const handleSave = async () => {
  if (!eventId?.value) {
    message.warning('请先选择活动')
    return
  }

  try {
    saving.value = true

    // 1. 先存天注册表，拿到带 id 的天列表
    const dayPayload = scheduleDays.value.map((day) => ({
      ...(day.backendId ? { id: day.backendId } : {}),
      scheduleTags: day.dateTag,
    }))
    const savedDaysResponse: any = await eventApi.saveSchedules(eventId.value, dayPayload)
    const savedDays = Array.isArray(savedDaysResponse) ? savedDaysResponse : []
    // 回填天 id 与 scheduleId
    scheduleDays.value.forEach((day, index) => {
      day.backendId = toBackendId(savedDays[index]?.id) ?? day.backendId
    })
    groups.value.forEach((group) => {
      group.itineraries.forEach((it) => {
        const day = dayById(it.dayId)
        it.scheduleId = day?.backendId ?? null
      })
    })

    // 2. 再存考察组（身份 + 各天行程）
    const groupPayload = groups.value.map((group) => ({
      ...(group.backendId ? { id: group.backendId } : {}),
      name: group.name,
      itineraries: group.itineraries
        .filter((it) => it.scheduleId !== null)
        .map((it) => {
          const day = dayById(it.dayId)
          return {
            ...(it.backendId ? { id: it.backendId } : {}),
            scheduleId: it.scheduleId,
            routeUrl: it.routeMapUrl || null,
            scheduleUrl: it.scheduleUrl || null,
            routeNode: it.nodes.map((node) => node.name).filter((name) => name.trim()),
            eventArrangements: it.activities.map((activity) => ({
              startTime: combineDateTime(day?.dateTag || '', activity.startTime),
              endTime: combineDateTime(day?.dateTag || '', activity.endTime),
              content: activity.content.trim(),
              location: activity.location.trim(),
            })).filter((arr) => arr.startTime || arr.endTime || arr.content || arr.location),
          }
        }),
    }))
    const savedGroupsResponse: any = await eventApi.saveInspectionTeams(eventId.value, groupPayload)
    const savedGroups = Array.isArray(savedGroupsResponse) ? savedGroupsResponse : []
    // 回填组与行程 id
    groups.value.forEach((group, index) => {
      const saved = savedGroups[index]
      if (!saved) return
      group.backendId = toBackendId(saved.id) ?? group.backendId
      const savedItineraries = Array.isArray(saved.itineraries) ? saved.itineraries : []
      group.itineraries.forEach((it) => {
        const matched = savedItineraries.find((raw: any) =>
          toBackendId(raw.scheduleId) === it.scheduleId,
        )
        if (matched) {
          it.backendId = toBackendId(matched.id) ?? it.backendId
        }
      })
    })

    message.success('保存成功')
  } catch (error) {
    message.error(getErrorMessage(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

function moveItem<T>(items: T[], index: number, offset: -1 | 1) {
  const targetIndex = index + offset;
  if (targetIndex < 0 || targetIndex >= items.length) {
    return;
  }

  const [item] = items.splice(index, 1);
  items.splice(targetIndex, 0, item);
}

function addDay() {
  scheduleDays.value.push(createDay());
  ensureItinerariesForAllGroups();
}

function removeDay(dayId: string) {
  scheduleDays.value = scheduleDays.value.filter((day) => day.id !== dayId);
  ensureItinerariesForAllGroups();
}

function addGroup() {
  groups.value.push(createGroup());
}

function removeGroup(groupIndex: number) {
  groups.value.splice(groupIndex, 1);
}

function moveGroup(groupIndex: number, offset: -1 | 1) {
  moveItem(groups.value, groupIndex, offset);
}

function addRouteNode(itinerary: Itinerary) {
  itinerary.nodes.push(createRouteNode());
}

function removeRouteNode(itinerary: Itinerary, nodeIndex: number) {
  itinerary.nodes.splice(nodeIndex, 1);
}

function moveRouteNode(itinerary: Itinerary, nodeIndex: number, offset: -1 | 1) {
  moveItem(itinerary.nodes, nodeIndex, offset);
}

function addActivity(itinerary: Itinerary) {
  itinerary.activities.push(createActivity());
}

function removeActivity(itinerary: Itinerary, activityIndex: number) {
  itinerary.activities.splice(activityIndex, 1);
}

function moveActivity(itinerary: Itinerary, activityIndex: number, offset: -1 | 1) {
  moveItem(itinerary.activities, activityIndex, offset);
}

async function handleLocalFileChange(event: Event, itinerary: Itinerary, field: UploadUrlField) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file || !eventId?.value) {
    input.value = "";
    return;
  }

  try {
    uploading.value = true;
    const response = await eventApi.uploadScheduleFile(file, {
      activityId: eventId.value,
      usageType: field === "routeMapUrl" ? "schedule-route" : "schedule-file",
    });
    const uploadedUrl = pickUploadUrl(response);

    if (!uploadedUrl) {
      message.warning("文件已上传，但接口未返回访问地址");
      return;
    }

    itinerary[field] = uploadedUrl;
    message.success("上传成功");
  } catch (error) {
    message.error(getErrorMessage(error, "上传失败"));
  } finally {
    uploading.value = false;
    input.value = "";
  }
}

onMounted(() => {
  if (eventId?.value) {
    loadScheduleData()
  } else {
    scheduleDays.value = [createDay("第一天")]
    groups.value = [createGroup()]
  }
})

watch(() => eventId?.value, (newId) => {
  if (newId) {
    loadScheduleData()
  }
})
</script>

<style scoped>
.schedule-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.day-card,
.group-card,
.sub-card {
  border-radius: 8px;
}

.day-card {
  background: #fbfcfe;
}

.group-card {
  background: #ffffff;
}

.sub-card {
  background: #fcfcfd;
}

.day-heading,
.group-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex-wrap: wrap;
}

.day-order,
.group-order {
  flex: 0 0 auto;
  color: #344054;
  font-size: 14px;
  font-weight: 650;
}

.date-tag-input {
  flex: 1 1 260px;
  width: min(360px, 48vw);
}

.url-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
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

.node-row,
.activity-row {
  display: grid;
  gap: 10px;
  align-items: center;
}

.node-row {
  grid-template-columns: 32px minmax(0, 1fr) auto;
}

.activity-row {
  grid-template-columns: 126px 12px 126px minmax(180px, 1fr) minmax(140px, 0.65fr) auto;
}

.row-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  color: #175cd3;
  font-size: 13px;
  font-weight: 650;
  background: #eff6ff;
}

.time-separator {
  color: #667085;
  text-align: center;
}

.row-actions {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

@media (max-width: 900px) {
  .activity-row {
    grid-template-columns: minmax(0, 1fr) 12px minmax(0, 1fr);
  }

  .activity-row > :nth-child(4),
  .activity-row > :nth-child(5),
  .activity-row > :nth-child(6) {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .day-heading,
  .group-heading,
  .url-control,
  .node-row {
    grid-template-columns: 1fr;
  }

  .day-heading,
  .group-heading {
    align-items: stretch;
  }

  .date-tag-input {
    flex-basis: 100%;
    width: 100%;
  }

  .node-row,
  .activity-row {
    grid-template-columns: 1fr;
  }

  .time-separator {
    display: none;
  }

  .row-actions {
    justify-content: flex-start;
  }
}
</style>
