<template>
  <n-space vertical :size="16" class="person-editor">
    <n-card title="分组设置" :bordered="false">
      <template #header-extra>
        <n-flex :size="8" align="center">
          <n-button secondary :loading="colorTagsLoading" @click="fetchColorTags">刷新颜色</n-button>
          <n-button type="primary" @click="addColorTag">
            <template #icon>
              <GoPlus />
            </template>
            增加类型
          </n-button>
        </n-flex>
      </template>

      <n-alert v-if="colorTagsError" type="warning" class="groups-alert" :show-icon="false">
        {{ colorTagsError }}
      </n-alert>

      <n-empty v-if="colorTags.length === 0" description="暂无类型颜色">
        <template #extra>
          <n-button type="primary" @click="addColorTag">增加类型</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="10">
        <div v-for="tag in colorTags" :key="tag.value" class="type-row">
          <span class="color-dot" :style="{ backgroundColor: tag.color }" />
          <n-input v-model:value="tag.name" placeholder="请输入类型名称" clearable />
          <n-color-picker v-model:value="tag.color" :show-alpha="false" />
          <n-button circle quaternary type="error" aria-label="删除类型" @click="removeColorTag(tag.value)">
            <template #icon>
              <GoX />
            </template>
          </n-button>
        </div>
      </n-space>
    </n-card>

    <n-card title="人员列表" :bordered="false">
      <template #header-extra>
        <n-flex :size="8" align="center">
          <n-switch v-model:value="groupByColor" size="small">
            <template #checked>按颜色分组</template>
            <template #unchecked>普通列表</template>
          </n-switch>
          <n-button secondary :loading="groupsLoading" @click="fetchInspectionGroups">刷新考察组</n-button>
          <n-button type="primary" @click="addPerson">
            <template #icon>
              <GoPlus />
            </template>
            添加人员
          </n-button>
        </n-flex>
      </template>

      <n-alert v-if="groupsError" type="warning" class="groups-alert" :show-icon="false">
        {{ groupsError }}
      </n-alert>

      <n-empty v-if="people.length === 0" description="暂无人员">
        <template #extra>
          <n-button type="primary" @click="addPerson">添加人员</n-button>
        </template>
      </n-empty>

      <div v-else-if="groupByColor" class="color-group-list">
        <section v-for="group in colorGroupedPeople" :key="group.key" class="color-group-panel">
          <div class="color-group-header">
            <span
              class="color-dot"
              :style="{ backgroundColor: group.color }"
            />
            <span class="color-group-title">{{ group.label }}</span>
            <span class="color-group-count">{{ group.people.length }}人</span>
          </div>

          <n-space vertical :size="10">
            <div
              v-for="person in group.people"
              :key="person.id"
              class="person-row"
              :style="{ '--person-color': getPersonColor(person) }"
            >
              <div class="move-actions">
                <n-button
                  circle
                  quaternary
                  :disabled="!canMovePerson(person.id, -1)"
                  aria-label="上移人员"
                  @click="movePersonById(person.id, -1)"
                >
                  <template #icon>
                    <GoArrowUp />
                  </template>
                </n-button>
                <n-button
                  circle
                  quaternary
                  :disabled="!canMovePerson(person.id, 1)"
                  aria-label="下移人员"
                  @click="movePersonById(person.id, 1)"
                >
                  <template #icon>
                    <GoArrowDown />
                  </template>
                </n-button>
              </div>

              <div class="person-fields">
                <n-input v-model:value="person.name" placeholder="姓名" clearable />
                <n-input v-model:value="person.organizationTitle" placeholder="单位" clearable />
                <n-input v-model:value="person.nickName" placeholder="昵称或展示名" clearable />
                <n-select
                  v-model:value="person.groupId"
                  :options="inspectionGroupOptions"
                  :loading="groupsLoading"
                  placeholder="考察组"
                  clearable
                />
                <n-select
                  v-model:value="person.colorTagId"
                  :options="colorTagOptions"
                  :loading="colorTagsLoading"
                  :disabled="colorTagOptions.length === 0"
                  placeholder="颜色分组"
                  clearable
                />
              </div>

              <n-button circle quaternary type="error" aria-label="删除人员" @click="removePersonById(person.id)">
                <template #icon>
                  <GoX />
                </template>
              </n-button>
            </div>
          </n-space>
        </section>
      </div>

      <n-space v-else vertical :size="10">
        <div
          v-for="(person, index) in people"
          :key="person.id"
          class="person-row"
          :style="{ '--person-color': getPersonColor(person) }"
        >
          <div class="move-actions">
            <n-button
              circle
              quaternary
              :disabled="index === 0"
              aria-label="上移人员"
              @click="movePerson(index, -1)"
            >
              <template #icon>
                <GoArrowUp />
              </template>
            </n-button>
            <n-button
              circle
              quaternary
              :disabled="index === people.length - 1"
              aria-label="下移人员"
              @click="movePerson(index, 1)"
            >
              <template #icon>
                <GoArrowDown />
              </template>
            </n-button>
          </div>

          <div class="person-fields">
            <n-input v-model:value="person.name" placeholder="姓名" clearable />
            <n-input v-model:value="person.organizationTitle" placeholder="单位" clearable />
            <n-input v-model:value="person.nickName" placeholder="昵称或展示名" clearable />
            <n-select
              v-model:value="person.groupId"
              :options="inspectionGroupOptions"
              :loading="groupsLoading"
              placeholder="考察组"
              clearable
            />
            <n-select
              v-model:value="person.colorTagId"
              :options="colorTagOptions"
              :loading="colorTagsLoading"
              :disabled="colorTagOptions.length === 0"
              placeholder="颜色分组"
              clearable
            />
          </div>

          <n-button circle quaternary type="error" aria-label="删除人员" @click="removePerson(index)">
            <template #icon>
              <GoX />
            </template>
          </n-button>
        </div>
      </n-space>
    </n-card>

    <n-flex justify="end" :size="12">
      <n-button @click="handleReset">重置</n-button>
      <n-button type="primary" size="large" :loading="saving" @click="handleSave">
        保存人员信息
      </n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoX } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface InspectionGroupOption {
  label: string;
  value: string;
}

interface ColorTagOption {
  label: string;
  value: string;
  backendId: number | null;
  name: string;
  color: string;
}

interface PersonRecord {
  id: string;
  backendId: number | null;
  name: string;
  organizationTitle: string;
  nickName: string;
  groupId: string | null;
  colorTagId: string | null;
}

type RawInspectionGroup = {
  id?: string | number;
  value?: string | number;
  groupId?: string | number;
  name?: string;
  label?: string;
  groupName?: string;
};

type RawColorTag = {
  id?: string | number;
  value?: string | number;
  name?: string;
  label?: string;
  color?: string;
};

const idCounters = {
  person: 1,
  colorTag: 1,
};

const DEFAULT_UNGROUPED_COLOR = "#d0d5dd";
const typeColors = ["#2563eb", "#16a34a", "#f59e0b", "#dc2626", "#7c3aed", "#0891b2"];

const inspectionGroups = ref<InspectionGroupOption[]>([]);
const colorTags = ref<ColorTagOption[]>([]);
const people = ref<PersonRecord[]>([createPerson()]);
const groupsLoading = ref(false);
const groupsError = ref("");
const colorTagsLoading = ref(false);
const colorTagsError = ref("");
const groupByColor = ref(true);
const deletedColorTagIds = ref<number[]>([]);
const loading = ref(false);
const saving = ref(false);

const inspectionGroupOptions = computed(() => inspectionGroups.value);
const colorTagOptions = computed(() =>
  colorTags.value.map((tag) => ({
    label: tag.name,
    value: tag.value,
  })),
);
const colorTagMap = computed(() => new Map(colorTags.value.map((tag) => [tag.value, tag])));
const colorGroupedPeople = computed(() => {
  const grouped = new Map<string, { key: string; label: string; color: string; people: PersonRecord[] }>();
  const ungrouped = {
    key: "__ungrouped__",
    label: "未分组",
    color: DEFAULT_UNGROUPED_COLOR,
    people: [] as PersonRecord[],
  };

  colorTags.value.forEach((tag) => {
    grouped.set(tag.value, {
      key: tag.value,
      label: tag.name,
      color: tag.color,
      people: [],
    });
  });

  people.value.forEach((person) => {
    const colorTag = person.colorTagId ? grouped.get(person.colorTagId) : null;
    if (colorTag) {
      colorTag.people.push(person);
    } else {
      ungrouped.people.push(person);
    }
  });

  const groups = Array.from(grouped.values()).filter((group) => group.people.length > 0);
  if (ungrouped.people.length > 0) {
    groups.push(ungrouped);
  }

  return groups;
});

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createPerson(values: Partial<Omit<PersonRecord, "id">> = {}): PersonRecord {
  return {
    id: nextId("person"),
    backendId: values.backendId ?? null,
    name: values.name ?? "",
    organizationTitle: values.organizationTitle ?? "",
    nickName: values.nickName ?? "",
    groupId: values.groupId ?? null,
    colorTagId: values.colorTagId ?? null,
  };
}

function createColorTag(name = "", color?: string, backendId: number | null = null, value?: string): ColorTagOption {
  return {
    label: name,
    value: value ?? (backendId !== null ? String(backendId) : nextId("colorTag")),
    backendId,
    name,
    color: color ?? typeColors[(idCounters.colorTag - 2) % typeColors.length],
  };
}

function addPerson() {
  people.value.push(createPerson());
}

function addColorTag() {
  colorTags.value.push(createColorTag(`类型 ${colorTags.value.length + 1}`));
}

function removeColorTag(colorTagId: string) {
  const tag = getColorTag(colorTagId);
  if (tag?.backendId !== null && tag?.backendId !== undefined) {
    deletedColorTagIds.value.push(tag.backendId);
  }

  colorTags.value = colorTags.value.filter((item) => item.value !== colorTagId);
  people.value.forEach((person) => {
    if (person.colorTagId === colorTagId) {
      person.colorTagId = null;
    }
  });
}

function removePerson(index: number) {
  people.value.splice(index, 1);
}

function findPersonIndex(personId: string) {
  return people.value.findIndex((person) => person.id === personId);
}

function removePersonById(personId: string) {
  const index = findPersonIndex(personId);
  if (index >= 0) {
    removePerson(index);
  }
}

function movePerson(index: number, offset: -1 | 1) {
  const targetIndex = index + offset;
  if (targetIndex < 0 || targetIndex >= people.value.length) {
    return;
  }

  const [person] = people.value.splice(index, 1);
  people.value.splice(targetIndex, 0, person);
}

function canMovePerson(personId: string, offset: -1 | 1) {
  const index = findPersonIndex(personId);
  const targetIndex = index + offset;

  return index >= 0 && targetIndex >= 0 && targetIndex < people.value.length;
}

function movePersonById(personId: string, offset: -1 | 1) {
  const index = findPersonIndex(personId);
  if (index >= 0) {
    movePerson(index, offset);
  }
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

function normalizeInspectionGroups(rawGroups: RawInspectionGroup[]): InspectionGroupOption[] {
  return rawGroups
    .map((group) => {
      const value = group.id ?? group.value ?? group.groupId;
      const label = group.name ?? group.label ?? group.groupName ?? String(value ?? "");

      if (value === null || value === undefined || !label) {
        return null;
      }

      return {
        label,
        value: String(value),
      };
    })
    .filter((group): group is InspectionGroupOption => group !== null);
}

function createColorTagOption(rawTag: RawColorTag): ColorTagOption | null {
  const backendId = normalizeBackendId(rawTag.id ?? rawTag.value);
  const name = String(rawTag.name ?? rawTag.label ?? "").trim();
  const color = String(rawTag.color ?? "").trim() || DEFAULT_UNGROUPED_COLOR;

  if (!name && backendId === null) {
    return null;
  }

  return createColorTag(
    name || `颜色 ${backendId}`,
    color,
    backendId,
    backendId !== null ? String(backendId) : name,
  );
}

function normalizeColorTags(response: any): ColorTagOption[] {
  const seen = new Set<string>();

  return pickList(response, "records", "rows", "list", "items", "colorTags", "data")
    .map((colorTag) => createColorTagOption(colorTag ?? {}))
    .filter((tag): tag is ColorTagOption => {
      if (!tag || seen.has(tag.value)) {
        return false;
      }

      seen.add(tag.value);
      return true;
    });
}

function ensureColorTagFromRaw(rawTag: RawColorTag | null | undefined) {
  if (!rawTag) return null;

  const tag = createColorTagOption(rawTag);
  if (!tag) return null;

  const matched = colorTags.value.find((item) => {
    if (tag.backendId !== null && item.backendId === tag.backendId) return true;

    return tag.name && item.name === tag.name;
  });

  if (matched) {
    if (tag.backendId !== null) matched.backendId = tag.backendId;
    if (tag.name) {
      matched.name = tag.name;
      matched.label = tag.label;
    }
    if (tag.color) matched.color = tag.color;

    return matched.value;
  }

  colorTags.value.push(tag);
  return tag.value;
}

function getColorTag(colorTagId: string | null) {
  return colorTagId ? colorTagMap.value.get(colorTagId) ?? null : null;
}

function getPersonColor(person: PersonRecord) {
  return getColorTag(person.colorTagId)?.color ?? "transparent";
}

function buildColorTagPayload(type: string) {
  return colorTags.value
    .filter((tag) => tag.name.trim())
    .map((tag) => ({
      ...(tag.backendId !== null ? { id: tag.backendId } : {}),
      name: tag.name.trim(),
      color: tag.color,
      type,
    }));
}

async function deleteRemovedColorTags() {
  const ids = Array.from(new Set(deletedColorTagIds.value));
  if (ids.length === 0) return;

  await eventApi.deleteColorTags(ids);
  deletedColorTagIds.value = [];
}

async function fetchInspectionGroups() {
  groupsLoading.value = true;
  groupsError.value = "";

  try {
    const groups = eventId?.value
      ? await eventApi.getInspectionTeamItems(eventId.value)
      : [];
    inspectionGroups.value = normalizeInspectionGroups(groups);
  } catch (error) {
    groupsError.value = error instanceof Error ? error.message : "考察组加载失败";
    inspectionGroups.value = [];
  } finally {
    groupsLoading.value = false;
  }
}

async function fetchColorTags() {
  colorTagsLoading.value = true;
  colorTagsError.value = "";

  try {
    const response = eventId?.value
      ? await eventApi.getColorTagsByActivity(eventId.value, 'PERSON')
      : [];
    colorTags.value = normalizeColorTags(response);
    deletedColorTagIds.value = [];
  } catch (error) {
    colorTagsError.value = error instanceof Error ? error.message : "颜色分组加载失败";
    colorTags.value = [];
  } finally {
    colorTagsLoading.value = false;
  }
}

const loadPersonData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const response = await eventApi.getPersonsByActivity(eventId.value)
    const persons = pickList(response, "records", "rows", "list", "items", "persons", "data")

    if (persons && persons.length > 0) {
      // 将后端数据转换为前端格式
      people.value = persons.map((person: any) =>
        createPerson({
          backendId: Number.isFinite(Number(person.id)) ? Number(person.id) : null,
          name: person.name || '',
          organizationTitle: person.unit || person.organizationTitle || '',
          nickName: person.nickName || '',
          groupId: person.inspectionTeamItemId ? String(person.inspectionTeamItemId) :
                   (person.groupId ? String(person.groupId) : null),
          colorTagId: ensureColorTagFromRaw(person.colorTag),
        })
      )
    } else {
      people.value = [createPerson()]
    }
  } catch (error: any) {
    message.error(error.message || '加载人员数据失败')
    people.value = [createPerson()]
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!eventId?.value) {
    message.warning('请先选择活动')
    return
  }

  try {
    saving.value = true

    const colorTagPayload = buildColorTagPayload("PERSON");
    // 将前端数据转换为后端格式
    const personsData = people.value
      .filter(person => person.name.trim())
      .map(person => {
        const colorTag = getColorTag(person.colorTagId);

        return {
          name: person.name.trim(),
          ...(person.backendId ? { id: person.backendId } : {}),
          unit: person.organizationTitle.trim(),
          nickName: person.nickName.trim(),
          ...(person.groupId ? { inspectionTeamItemId: Number(person.groupId) } : {}),
          ...(colorTag
            ? {
                colorTag: {
                  ...(colorTag.backendId !== null ? { id: colorTag.backendId } : {}),
                  name: colorTag.name,
                  color: colorTag.color,
                  type: "PERSON",
                },
              }
            : {}),
        };
      })

    if (
      personsData.length === 0 &&
      colorTagPayload.length === 0 &&
      deletedColorTagIds.value.length === 0
    ) {
      message.warning('请至少添加一个人员')
      return
    }

    if (colorTagPayload.length > 0) {
      await eventApi.saveColorTags(eventId.value, colorTagPayload);
    }
    if (personsData.length > 0) {
      await eventApi.savePersons(eventId.value, personsData)
    }
    await deleteRemovedColorTags()
    message.success('保存成功')
    await loadPageData()
  } catch (error: any) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  if (eventId?.value) {
    void loadPageData()
  } else {
    people.value = [createPerson()]
  }
}

async function loadPageData() {
  if (!eventId?.value) return;

  await Promise.all([
    fetchInspectionGroups(),
    fetchColorTags(),
  ]);
  await loadPersonData();
}

onMounted(() => {
  if (eventId?.value) {
    void loadPageData()
  }
});

watch(() => eventId?.value, (newId) => {
  if (newId) {
    void loadPageData()
  } else {
    inspectionGroups.value = [];
    colorTags.value = [];
    people.value = [createPerson()];
  }
})
</script>

<style scoped>
.person-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.type-row,
.person-row {
  display: grid;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #ffffff;
}

.type-row {
  grid-template-columns: 16px minmax(180px, 1fr) 180px auto;
}

.person-row {
  border-left: 4px solid var(--person-color, transparent);
}

.person-row {
  grid-template-columns: auto minmax(0, 1fr) auto;
}

.move-actions {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
}

.person-fields {
  display: grid;
  grid-template-columns:
    minmax(110px, 0.75fr)
    minmax(180px, 1.15fr)
    minmax(130px, 0.85fr)
    minmax(140px, 0.9fr)
    minmax(140px, 0.9fr);
  gap: 10px;
}

.groups-alert {
  margin-bottom: 12px;
}

.color-group-list {
  display: grid;
  gap: 14px;
}

.color-group-panel {
  display: grid;
  gap: 10px;
}

.color-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 28px;
  padding: 0 2px;
}

.color-dot {
  width: 12px;
  height: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 50%;
  flex: 0 0 auto;
}

.color-group-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.color-group-count {
  padding: 1px 7px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #667085;
  font-size: 12px;
  line-height: 20px;
}

@media (max-width: 900px) {
  .person-fields {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .type-row,
  .person-row,
  .person-fields {
    grid-template-columns: 1fr;
  }

  .move-actions {
    flex-direction: row;
  }
}
</style>
