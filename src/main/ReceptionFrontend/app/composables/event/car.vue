<template>
  <n-space vertical :size="16" class="car-editor">
    <n-card title="乘车安排" :bordered="false">
      <template #header-extra>
        <n-flex :size="8" align="center">
          <n-button secondary :loading="peopleLoading" @click="fetchPeople">刷新人员</n-button>
          <n-button type="primary" @click="addVehicle">
            <template #icon>
              <GoPlus />
            </template>
            添加车辆
          </n-button>
        </n-flex>
      </template>

      <n-alert v-if="peopleError" type="warning" class="status-alert" :show-icon="false">
        {{ peopleError }}
      </n-alert>

      <n-spin :show="loading">
        <n-empty v-if="vehicles.length === 0" description="暂无车辆安排">
          <template #extra>
            <n-button type="primary" @click="addVehicle">添加车辆</n-button>
          </template>
        </n-empty>

      <n-space v-else vertical :size="14">
        <section v-for="(vehicle, vehicleIndex) in vehicles" :key="vehicle.id" class="vehicle-panel">
          <div class="vehicle-header">
            <div class="vehicle-title">
              <span class="vehicle-order">车辆 {{ vehicleIndex + 1 }}</span>
              <span class="vehicle-summary">{{ getVehicleSummary(vehicle) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="vehicleIndex === 0"
                aria-label="上移车辆"
                @click="moveVehicle(vehicleIndex, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="vehicleIndex === vehicles.length - 1"
                aria-label="下移车辆"
                @click="moveVehicle(vehicleIndex, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除车辆" @click="removeVehicle(vehicleIndex)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1 s:2 m:4" :x-gap="14" :y-gap="12" responsive="screen">
              <n-form-item-gi label="车号">
                <n-input v-model:value="vehicle.carNo" placeholder="如 1 号车" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="车牌号">
                <n-input v-model:value="vehicle.plateNumber" placeholder="请输入车牌号" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="司机姓名">
                <n-input v-model:value="vehicle.driverName" placeholder="请输入司机姓名" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="司机电话">
                <n-input v-model:value="vehicle.driverPhone" placeholder="请输入司机电话" clearable />
              </n-form-item-gi>
            </n-grid>
          </n-form>

          <n-divider />

          <div class="section-heading">
            <span>随车工作人员</span>
            <n-button size="small" secondary type="primary" @click="addStaff(vehicle)">
              <template #icon>
                <GoPlus />
              </template>
              添加
            </n-button>
          </div>

          <n-empty v-if="vehicle.staff.length === 0" description="暂无随车工作人员" size="small" />

          <n-space v-else vertical :size="8">
            <div v-for="(staff, staffIndex) in vehicle.staff" :key="staff.id" class="staff-row">
              <n-input v-model:value="staff.name" placeholder="姓名" clearable />
              <n-input v-model:value="staff.phone" placeholder="电话" clearable />
              <n-button
                circle
                quaternary
                type="error"
                aria-label="删除随车工作人员"
                @click="removeStaff(vehicle, staffIndex)"
              >
                <template #icon>
                  <GoX />
                </template>
              </n-button>
            </div>
          </n-space>

          <n-divider />

          <div class="section-heading">
            <span>乘车人员</span>
            <span class="section-count">{{ vehicle.passengerIds.length }} 人</span>
          </div>

          <div v-if="vehicle.passengerIds.length > 0" class="passenger-tags">
            <n-tag
              v-for="personId in vehicle.passengerIds"
              :key="personId"
              round
              closable
              @close="removePassenger(vehicle, personId)"
            >
              {{ getPersonLabel(personId) }}
            </n-tag>
          </div>
          <n-empty v-else description="暂无乘车人员" size="small" />

          <div class="passenger-picker">
            <n-select
              v-model:value="vehicle.pendingPassengerId"
              filterable
              clearable
              :loading="peopleLoading"
              :options="availablePassengerOptions"
              placeholder="从后台人员列表选择"
              :disabled="peopleLoading || people.length === 0 || availablePassengerOptions.length === 0"
            />
            <n-button
              type="primary"
              secondary
              :disabled="!vehicle.pendingPassengerId"
              @click="addPassenger(vehicle)"
            >
              添加乘车人员
            </n-button>
          </div>
        </section>
      </n-space>
      </n-spin>
    </n-card>

    <n-flex justify="end" class="footer-actions">
      <n-button secondary @click="addVehicle">
        <template #icon>
          <GoPlus />
        </template>
        添加车辆
      </n-button>
      <n-button type="primary" :loading="saving" @click="saveCarArrangements">保存乘车</n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import type { SelectOption } from "naive-ui";
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash, GoX } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface PersonOption {
  id: string;
  name: string;
  phone: string;
  organizationTitle: string;
}

interface StaffRecord {
  id: string;
  name: string;
  phone: string;
}

interface VehicleArrangement {
  id: string;
  backendId: number | null;
  carNo: string;
  plateNumber: string;
  driverName: string;
  driverPhone: string;
  staff: StaffRecord[];
  passengerIds: string[];
  pendingPassengerId: string | null;
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

const idCounters = {
  vehicle: 1,
  staff: 1,
};

const people = ref<PersonOption[]>([]);
const peopleLoading = ref(false);
const peopleError = ref("");
const saving = ref(false);
const loading = ref(false);
const vehicles = ref<VehicleArrangement[]>([]);

const personMap = computed(() => new Map(people.value.map((person) => [person.id, person])));

const assignedPassengerIds = computed(() => {
  const ids = new Set<string>();
  vehicles.value.forEach((vehicle) => {
    vehicle.passengerIds.forEach((personId) => ids.add(personId));
  });

  return ids;
});

const availablePassengerOptions = computed<SelectOption[]>(() =>
  people.value
    .filter((person) => !assignedPassengerIds.value.has(person.id))
    .map((person) => ({
      label: getPersonOptionLabel(person),
      value: person.id,
    })),
);

function nextId(prefix: keyof typeof idCounters) {
  const nextValue = idCounters[prefix];
  idCounters[prefix] += 1;

  return `${prefix}-${nextValue}`;
}

function createStaff(): StaffRecord {
  return {
    id: nextId("staff"),
    name: "",
    phone: "",
  };
}

function createVehicle(values: Partial<Omit<VehicleArrangement, "id" | "staff" | "passengerIds" | "pendingPassengerId">> & {
  staff?: StaffRecord[];
  passengerIds?: string[];
  pendingPassengerId?: string | null;
} = {}): VehicleArrangement {
  return {
    id: nextId("vehicle"),
    backendId: values.backendId ?? null,
    carNo: values.carNo ?? "",
    plateNumber: values.plateNumber ?? "",
    driverName: values.driverName ?? "",
    driverPhone: values.driverPhone ?? "",
    staff: values.staff ?? [],
    passengerIds: values.passengerIds ?? [],
    pendingPassengerId: values.pendingPassengerId ?? null,
  };
}

function addVehicle() {
  vehicles.value.push(createVehicle());
}

function removeVehicle(vehicleIndex: number) {
  vehicles.value.splice(vehicleIndex, 1);
}

function moveItem<T>(items: T[], index: number, offset: -1 | 1) {
  const targetIndex = index + offset;
  if (targetIndex < 0 || targetIndex >= items.length) {
    return;
  }

  const [item] = items.splice(index, 1);
  items.splice(targetIndex, 0, item);
}

function moveVehicle(vehicleIndex: number, offset: -1 | 1) {
  moveItem(vehicles.value, vehicleIndex, offset);
}

function addStaff(vehicle: VehicleArrangement) {
  vehicle.staff.push(createStaff());
}

function removeStaff(vehicle: VehicleArrangement, staffIndex: number) {
  vehicle.staff.splice(staffIndex, 1);
}

function addPassenger(vehicle: VehicleArrangement) {
  const personId = vehicle.pendingPassengerId;
  if (!personId || assignedPassengerIds.value.has(personId)) {
    vehicle.pendingPassengerId = null;
    return;
  }

  vehicle.passengerIds.push(personId);
  vehicle.pendingPassengerId = null;
}

function removePassenger(vehicle: VehicleArrangement, personId: string) {
  vehicle.passengerIds = vehicle.passengerIds.filter((id) => id !== personId);
}

function getVehicleSummary(vehicle: VehicleArrangement) {
  const summaryParts = [vehicle.carNo, vehicle.plateNumber, vehicle.driverName].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写车辆信息";
}

function getPersonOptionLabel(person: PersonOption) {
  const detailParts = [person.organizationTitle, person.phone].filter(Boolean);

  return detailParts.length > 0 ? `${person.name}（${detailParts.join(" / ")}）` : person.name;
}

function getPersonLabel(personId: string) {
  const person = personMap.value.get(personId);

  return person ? getPersonOptionLabel(person) : personId;
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

function normalizeBackendId(value: unknown) {
  const id = Number(value);

  return Number.isFinite(id) ? id : null;
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
    clearUnavailablePassengers();
  } catch (error) {
    people.value = [];
    peopleError.value = getErrorMessage(error, "人员列表加载失败");
  } finally {
    peopleLoading.value = false;
  }
}

const loadCarData = async () => {
  if (!eventId?.value) return

  loading.value = true
  try {
    const cars = await eventApi.getCarsByActivity(eventId.value)

    if (cars && cars.length > 0) {
      vehicles.value = cars.map((car: any) => {
        const vehicle = createVehicle({
          backendId: normalizeBackendId(car.id),
          carNo: car.carNumber ? String(car.carNumber) : '',
          plateNumber: car.carPlate || '',
          driverName: car.driver || '',
          driverPhone: car.driverNumber || '',
          staff: (car.passengersOnBoardList || []).map((s: any) => ({
            id: nextId('staff'),
            name: s.name || '',
            phone: s.phone || '',
          })),
        })

        if (car.passengersList && Array.isArray(car.passengersList)) {
          vehicle.passengerIds = car.passengersList
            .map((p: any) => {
              if (p.id) return String(p.id)
              const foundPerson = people.value.find(person => person.name === p.name)
              return foundPerson ? foundPerson.id : null
            })
            .filter((id: string | null) => id !== null) as string[]
        }

        return vehicle
      })
    } else {
      vehicles.value = [createVehicle()]
    }
  } catch (error: any) {
    message.error(error.message || '加载车辆数据失败')
    vehicles.value = [createVehicle()]
  } finally {
    loading.value = false
  }
}

function clearUnavailablePassengers() {
  const availableIds = new Set(people.value.map((person) => person.id));
  vehicles.value.forEach((vehicle) => {
    vehicle.passengerIds = vehicle.passengerIds.filter((personId) => availableIds.has(personId));
    if (vehicle.pendingPassengerId && !availableIds.has(vehicle.pendingPassengerId)) {
      vehicle.pendingPassengerId = null;
    }
  });
}

function buildSavePayload() {
  return {
    vehicles: vehicles.value
      .map((vehicle) => {
        const parsedCarNumber = vehicle.carNo ? Number(vehicle.carNo) : null;
        const carNumber = Number.isFinite(parsedCarNumber) ? parsedCarNumber : null;
        const carPlate = vehicle.plateNumber.trim();
        const driver = vehicle.driverName.trim();
        const driverNumber = vehicle.driverPhone.trim();
        const passengersOnBoardList = vehicle.staff
        .map((staff) => ({
          name: staff.name.trim(),
          phone: staff.phone.trim(),
        }))
          .filter((staff) => staff.name || staff.phone);
        const passengersList = vehicle.passengerIds
          .map(personId => {
            const person = personMap.value.get(personId)
            if (!person) return null
            return {
              id: Number(personId),
              name: person.name,
              unit: person.organizationTitle || null,
              nickName: null,
              inspectionTeamItemId: null,
            }
          })
          .filter(p => p !== null);

        if (
          !vehicle.backendId &&
          !carNumber &&
          !carPlate &&
          !driver &&
          !driverNumber &&
          passengersOnBoardList.length === 0 &&
          passengersList.length === 0
        ) {
          return null;
        }

        return {
          ...(vehicle.backendId ? { id: vehicle.backendId } : {}),
          carNumber,
          carPlate,
          driver,
          driverNumber,
          passengersOnBoardList,
          passengersList,
        };
      })
      .filter((vehicle): vehicle is NonNullable<typeof vehicle> => vehicle !== null),
  };
}

async function saveCarArrangements() {
  if (!eventId?.value) {
    message.warning('请先选择活动');
    return;
  }

  saving.value = true;

  try {
    const payload = buildSavePayload();
    await eventApi.saveCars(eventId.value, payload.vehicles);

    await loadCarData();
    message.success('乘车安排已保存');
  } catch (error) {
    message.error(getErrorMessage(error, '乘车安排保存失败'));
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (eventId?.value) {
    void fetchPeople();
    loadCarData();
  } else {
    vehicles.value = [createVehicle()]
  }
});

watch(() => eventId?.value, (newId) => {
  if (newId) {
    void fetchPeople();
    loadCarData();
  }
})
</script>

<style scoped>
.car-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.status-alert {
  margin-bottom: 12px;
}

.vehicle-panel {
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.vehicle-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.vehicle-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.vehicle-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.vehicle-summary {
  min-width: 0;
  overflow: hidden;
  color: #475467;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #344054;
  font-size: 14px;
  font-weight: 650;
}

.section-count {
  color: #667085;
  font-size: 13px;
  font-weight: 500;
}

.staff-row {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(160px, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.passenger-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.passenger-picker {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.footer-actions {
  padding-bottom: 4px;
}

@media (max-width: 720px) {
  .vehicle-header,
  .vehicle-title,
  .section-heading {
    align-items: flex-start;
  }

  .vehicle-header {
    flex-direction: column;
  }

  .vehicle-title {
    width: 100%;
    flex-direction: column;
    gap: 4px;
  }

  .vehicle-summary {
    width: 100%;
    white-space: normal;
  }

  .staff-row,
  .passenger-picker {
    grid-template-columns: 1fr;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
