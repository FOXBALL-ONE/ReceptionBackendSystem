<template>
  <n-space vertical :size="16" class="lodging-editor">
    <n-card title="分组设置" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addPersonType">
          <template #icon>
            <GoPlus />
          </template>
          增加类型
        </n-button>
      </template>

      <n-empty v-if="personTypes.length === 0" description="暂无类型">
        <template #extra>
          <n-button type="primary" @click="addPersonType">增加类型</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="10">
        <div v-for="type in personTypes" :key="type.id" class="type-row">
          <span class="color-dot" :style="{ backgroundColor: type.color }" />
          <n-input v-model:value="type.name" placeholder="请输入类型名称" clearable />
          <n-color-picker v-model:value="type.color" :show-alpha="false" />
          <n-button circle quaternary type="error" aria-label="删除类型" @click="removePersonType(type.id)">
            <template #icon>
              <GoX />
            </template>
          </n-button>
        </div>
      </n-space>
    </n-card>

    <n-card title="住宿列表" :bordered="false">
      <template #header-extra>
        <n-flex :size="8" align="center">
          <n-button secondary :loading="peopleLoading" @click="fetchPeople">刷新人员</n-button>
          <n-button type="primary" @click="addLodging">
            <template #icon>
              <GoPlus />
            </template>
            手动添加
          </n-button>
        </n-flex>
      </template>

      <n-alert v-if="peopleError" type="warning" class="status-alert" :show-icon="false">
        {{ peopleError }}
      </n-alert>

      <n-alert v-if="saveStatus.message" :type="saveStatus.type" class="status-alert" :show-icon="false">
        {{ saveStatus.message }}
      </n-alert>

      <div class="person-toolbar">
        <n-select
          v-model:value="pendingPersonId"
          filterable
          clearable
          :loading="peopleLoading"
          :options="availablePersonOptions"
          placeholder="从人员列表选择"
          :disabled="peopleLoading || people.length === 0 || availablePersonOptions.length === 0"
        />
        <n-button type="primary" secondary :disabled="!pendingPersonId" @click="addFromPerson">
          <template #icon>
            <GoPlus />
          </template>
          从人员添加
        </n-button>
      </div>

      <n-empty v-if="lodgings.length === 0" description="暂无住宿安排">
        <template #extra>
          <n-button type="primary" @click="addLodging">手动添加</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="14">
        <section v-for="(lodging, index) in lodgings" :key="lodging.id" class="lodging-panel">
          <div class="lodging-header">
            <div class="lodging-title">
              <span class="lodging-order">住宿 {{ index + 1 }}</span>
              <span
                v-if="getPersonType(lodging.typeId)"
                class="type-badge"
                :style="{ '--type-color': getPersonType(lodging.typeId)?.color }"
              >
                {{ getPersonType(lodging.typeId)?.name }}
              </span>
              <span class="lodging-summary">{{ getLodgingSummary(lodging) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="index === 0"
                aria-label="上移住宿"
                @click="moveLodging(index, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="index === lodgings.length - 1"
                aria-label="下移住宿"
                @click="moveLodging(index, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除住宿" @click="removeLodging(index)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1 s:2 m:4" :x-gap="14" :y-gap="12" responsive="screen">
              <n-form-item-gi label="房号">
                <n-input v-model:value="lodging.roomNumber" placeholder="请输入房号" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="姓名">
                <n-input v-model:value="lodging.name" placeholder="请输入姓名" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="类型">
                <n-select
                  v-model:value="lodging.typeId"
                  :options="personTypeOptions"
                  placeholder="类型选择"
                  clearable
                />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </section>
      </n-space>
    </n-card>

    <n-flex justify="end" class="footer-actions">
      <n-button secondary @click="addLodging">
        <template #icon>
          <GoPlus />
        </template>
        添加住宿
      </n-button>
      <n-button type="primary" :loading="saving" @click="saveLodgings">保存住宿</n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import type { SelectOption } from "naive-ui";
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash, GoX } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface PersonType {
  id: string;
  backendId: number | null;
  name: string;
  color: string;
}

interface PersonOption {
  id: string;
  name: string;
  phone: string;
  organizationTitle: string;
}

interface LodgingRecord {
  id: string;
  backendId: number | null;
  personId: string | null;
  roomNumber: string;
  name: string;
  typeId: string | null;
}

interface SaveStatus {
  type: "success" | "warning" | "error" | "info";
  message: string;
}

type RawPerson = {
  id?: string | number;
  value?: string | number;
  personId?: string | number;
  userId?: string | number;
  name?: string;
  label?: string;
  personName?: string;
  realName?: string;
  phone?: string;
  mobile?: string;
  tel?: string;
  telephone?: string;
  organizationTitle?: string;
  organization?: string;
  unit?: string;
  title?: string;
};

type PeopleResponse =
  | RawPerson[]
  | {
      records?: RawPerson[];
      rows?: RawPerson[];
      list?: RawPerson[];
      items?: RawPerson[];
      data?: RawPerson[];
    };

const typeColors = ["#2563eb", "#16a34a", "#f59e0b", "#dc2626", "#7c3aed", "#0891b2"];

const idCounters = {
  lodging: 1,
  type: 1,
};

const personTypes = ref<PersonType[]>(createDefaultPersonTypes());
const people = ref<PersonOption[]>([]);
const peopleLoading = ref(false);
const peopleError = ref("");
const pendingPersonId = ref<string | null>(null);
const lodgings = ref<LodgingRecord[]>([createLodging()]);
const deletedPersonTypeIds = ref<number[]>([]);
const saveStatus = ref<SaveStatus>({
  type: "info",
  message: "",
});
const loading = ref(false);
const saving = ref(false);

const assignedPersonIds = computed(() => {
  const ids = new Set<string>();
  lodgings.value.forEach((lodging) => {
    if (lodging.personId) {
      ids.add(lodging.personId);
    }
  });

  return ids;
});

const personTypeOptions = computed<SelectOption[]>(() =>
  personTypes.value
    .filter((type) => type.name.trim())
    .map((type) => ({
      label: type.name,
      value: type.id,
    })),
);

const availablePersonOptions = computed<SelectOption[]>(() =>
  people.value
    .filter((person) => !assignedPersonIds.value.has(person.id))
    .map((person) => ({
      label: getPersonOptionLabel(person),
      value: person.id,
    })),
);

const personMap = computed(() => new Map(people.value.map((person) => [person.id, person])));

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createPersonType(name = "", color?: string, backendId: number | null = null): PersonType {
  return {
    id: nextId("type"),
    backendId,
    name,
    color: color ?? typeColors[(idCounters.type - 2) % typeColors.length],
  };
}

function createDefaultPersonTypes() {
  return [
    createPersonType("工作人员", "#2563eb"),
    createPersonType("嘉宾", "#16a34a"),
  ];
}

function createLodging(values: Partial<Omit<LodgingRecord, "id">> = {}): LodgingRecord {
  return {
    id: nextId("lodging"),
    backendId: values.backendId ?? null,
    personId: values.personId ?? null,
    roomNumber: values.roomNumber ?? "",
    name: values.name ?? "",
    typeId: values.typeId ?? personTypes.value[0]?.id ?? null,
  };
}

function addPersonType() {
  personTypes.value.push(createPersonType(`类型 ${personTypes.value.length + 1}`));
}

function removePersonType(typeId: string) {
  const type = getPersonType(typeId);
  if (type?.backendId !== null && type?.backendId !== undefined) {
    deletedPersonTypeIds.value.push(type.backendId);
  }

  personTypes.value = personTypes.value.filter((type) => type.id !== typeId);
  lodgings.value.forEach((lodging) => {
    if (lodging.typeId === typeId) {
      lodging.typeId = null;
    }
  });
}

function addLodging() {
  lodgings.value.push(createLodging());
  saveStatus.value.message = "";
}

function addFromPerson() {
  const personId = pendingPersonId.value;
  const person = personId ? personMap.value.get(personId) : null;
  if (!person || assignedPersonIds.value.has(person.id)) {
    pendingPersonId.value = null;
    return;
  }

  lodgings.value.push(createLodging({
    personId: person.id,
    name: person.name,
  }));
  pendingPersonId.value = null;
  saveStatus.value.message = "";
}

function removeLodging(index: number) {
  lodgings.value.splice(index, 1);
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

function moveLodging(index: number, offset: -1 | 1) {
  moveItem(lodgings.value, index, offset);
  saveStatus.value.message = "";
}

function getPersonType(typeId: string | null) {
  return personTypes.value.find((type) => type.id === typeId) ?? null;
}

function getLodgingSummary(lodging: LodgingRecord) {
  const summaryParts = [lodging.roomNumber, lodging.name].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写住宿信息";
}

function getPersonOptionLabel(person: PersonOption) {
  const detailParts = [person.organizationTitle, person.phone].filter(Boolean);

  return detailParts.length > 0 ? `${person.name}（${detailParts.join(" / ")}）` : person.name;
}

function pickPeopleList(response: PeopleResponse): RawPerson[] {
  if (Array.isArray(response)) {
    return response;
  }

  return response.records ?? response.rows ?? response.list ?? response.items ?? response.data ?? [];
}

function normalizePeople(response: PeopleResponse): PersonOption[] {
  return pickPeopleList(response)
    .map((person) => {
      const id = person.id ?? person.value ?? person.personId ?? person.userId;
      const name = person.name ?? person.label ?? person.personName ?? person.realName ?? "";

      if (id === null || id === undefined || !name.trim()) {
        return null;
      }

      return {
        id: String(id),
        name: name.trim(),
        phone: String(person.phone ?? person.mobile ?? person.tel ?? person.telephone ?? ""),
        organizationTitle: String(person.organizationTitle ?? person.organization ?? person.unit ?? person.title ?? ""),
      };
    })
    .filter((person): person is PersonOption => person !== null);
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

function ensurePersonTypeFromColorTag(colorTag: any) {
  if (!colorTag) return null;

  const backendId = normalizeBackendId(colorTag.id);
  const name = String(colorTag.name ?? "").trim();
  const color = String(colorTag.color ?? "").trim() || undefined;
  const matchedType = personTypes.value.find((type) => {
    if (backendId !== null && type.backendId === backendId) return true;

    return name && type.name === name;
  });

  if (matchedType) {
    if (backendId !== null) matchedType.backendId = backendId;
    if (name) matchedType.name = name;
    if (color) matchedType.color = color;

    return matchedType.id;
  }

  if (!name) return null;

  const type = createPersonType(name, color, backendId);
  personTypes.value.push(type);

  return type.id;
}

function normalizeColorTags(response: any): PersonType[] {
  deletedPersonTypeIds.value = [];

  return pickList(response, "records", "rows", "list", "items", "colorTags", "data")
    .map((colorTag) => {
      const name = String(colorTag?.name ?? "").trim();
      if (!name) return null;

      return createPersonType(
        name,
        String(colorTag?.color ?? "").trim() || undefined,
        normalizeBackendId(colorTag?.id),
      );
    })
    .filter((type): type is PersonType => type !== null);
}

function normalizeLodgings(response: any): LodgingRecord[] {
  return pickList(response, "records", "rows", "list", "items", "lodgings", "data")
    .map((lodging) => {
      const person = lodging?.person ?? {};
      const personId = person.id ?? lodging?.personId;
      const typeId = ensurePersonTypeFromColorTag(lodging?.colorTag);

      return createLodging({
        backendId: normalizeBackendId(lodging?.id),
        personId: personId !== null && personId !== undefined ? String(personId) : null,
        roomNumber: String(lodging?.roomNumber ?? "").trim(),
        name: String(person.name ?? lodging?.name ?? "").trim(),
        typeId,
      });
    });
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

async function fetchPeople() {
  if (!eventId?.value) {
    peopleError.value = '未选择活动';
    return;
  }

  peopleLoading.value = true;
  peopleError.value = "";

  try {
    const response = await eventApi.getPersonsByActivity(eventId.value);
    people.value = normalizePeople(response);
    clearUnavailablePeople();
  } catch (error) {
    people.value = [];
    peopleError.value = getErrorMessage(error, "人员列表加载失败");
  } finally {
    peopleLoading.value = false;
  }
}

const loadLodgingData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const [colorTagsResponse, lodgingsResponse] = await Promise.all([
      eventApi.getColorTagsByActivity(eventId.value, 'LODGING'),
      eventApi.getLodgingsByActivity(eventId.value),
    ])
    const colorTags = normalizeColorTags(colorTagsResponse)

    personTypes.value = colorTags.length > 0 ? colorTags : createDefaultPersonTypes()

    const loadedLodgings = normalizeLodgings(lodgingsResponse)
    lodgings.value = loadedLodgings.length > 0 ? loadedLodgings : [createLodging()]
    saveStatus.value.message = ""
  } catch (error: any) {
    message.error(error.message || '加载住宿数据失败')
    lodgings.value = [createLodging()]
  } finally {
    loading.value = false
  }
}

function clearUnavailablePeople() {
  const availableIds = new Set(people.value.map((person) => person.id));
  lodgings.value.forEach((lodging) => {
    if (lodging.personId && !availableIds.has(lodging.personId)) {
      lodging.personId = null;
    }
  });

  if (pendingPersonId.value && !availableIds.has(pendingPersonId.value)) {
    pendingPersonId.value = null;
  }
}

function buildSavePayload() {
  const lodgingPayload = lodgings.value
    .map((lodging) => {
      const type = getPersonType(lodging.typeId);
      const selectedPerson = lodging.personId ? personMap.value.get(lodging.personId) : null;
      const roomNumber = lodging.roomNumber.trim();
      const personName = selectedPerson?.name ?? lodging.name.trim();
      const person = selectedPerson || lodging.personId || personName
        ? {
            ...(lodging.personId ? { id: Number(lodging.personId) } : {}),
            name: personName,
            unit: selectedPerson?.organizationTitle || null,
            nickName: null,
            inspectionTeamItemId: null,
          }
        : null;

      if (!lodging.backendId && !roomNumber && !person) {
        return null;
      }

      return {
        ...(lodging.backendId ? { id: lodging.backendId } : {}),
        roomNumber,
        ...(person ? { person } : {}),
        ...(type
          ? {
              colorTag: {
                ...(type.backendId ? { id: type.backendId } : {}),
                name: type.name.trim(),
                color: type.color,
                type: "LODGING",
              },
            }
          : {}),
      };
    })
    .filter((lodging): lodging is NonNullable<typeof lodging> => lodging !== null);

  return {
    colorTags: personTypes.value
      .filter((type) => type.name.trim())
      .map((type) => ({
        ...(type.backendId ? { id: type.backendId } : {}),
        name: type.name.trim(),
        color: type.color,
        type: "LODGING",
      })),
    lodgings: lodgingPayload,
  };
}

async function deleteRemovedPersonTypes() {
  const ids = Array.from(new Set(deletedPersonTypeIds.value));
  if (ids.length === 0) return;

  await eventApi.deleteColorTags(ids);
  deletedPersonTypeIds.value = [];
}

async function saveLodgings() {
  if (!eventId?.value) {
    saveStatus.value = {
      type: "warning",
      message: "请先选择活动",
    };
    return;
  }

  try {
    saving.value = true;

    const lodgingData = buildSavePayload();
    await eventApi.saveLodgings(eventId.value, lodgingData.lodgings, lodgingData.colorTags);
    await deleteRemovedPersonTypes();

    await loadLodgingData();
    saveStatus.value = {
      type: "success",
      message: "住宿安排已保存",
    };
  } catch (error: any) {
    saveStatus.value = {
      type: "error",
      message: error.message || "住宿安排保存失败",
    };
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (eventId?.value) {
    void fetchPeople();
    loadLodgingData();
  }
});

watch(() => eventId?.value, (newId) => {
  if (newId) {
    void fetchPeople();
    loadLodgingData();
  }
})
</script>

<style scoped>
.lodging-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.type-row,
.lodging-panel {
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

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}

.status-alert {
  margin-bottom: 12px;
}

.person-toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 12px;
  margin-bottom: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #f8fafc;
}

.lodging-panel {
  gap: 14px;
  padding: 14px;
  background: #fbfcfe;
}

.lodging-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.lodging-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.lodging-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.type-badge {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 140px;
  overflow: hidden;
  color: #344054;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-badge::before {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--type-color);
  content: "";
}

.lodging-summary {
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
  .type-row,
  .person-toolbar {
    grid-template-columns: 1fr;
  }

  .lodging-header,
  .lodging-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .lodging-title,
  .lodging-summary {
    width: 100%;
  }

  .lodging-summary {
    white-space: normal;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
