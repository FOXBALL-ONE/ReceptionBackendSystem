<template>
  <n-space vertical :size="16" class="dines-editor">
    <n-card title="用餐安排" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addMeal">
          <template #icon>
            <GoPlus />
          </template>
          添加
        </n-button>
      </template>

      <n-alert v-if="saveStatus.message" :type="saveStatus.type" class="status-alert" :show-icon="false">
        {{ saveStatus.message }}
      </n-alert>

      <n-empty v-if="meals.length === 0" description="暂无用餐安排">
        <template #extra>
          <n-button type="primary" @click="addMeal">添加用餐</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="12">
        <section v-for="(meal, index) in meals" :key="meal.id" class="meal-panel">
          <div class="meal-header">
            <div class="meal-title">
              <span class="meal-order">用餐 {{ index + 1 }}</span>
              <span class="meal-summary">{{ getMealSummary(meal) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="index === 0"
                aria-label="上移用餐安排"
                @click="moveMeal(index, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="index === meals.length - 1"
                aria-label="下移用餐安排"
                @click="moveMeal(index, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除用餐安排" @click="removeMeal(index)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1 s:2 m:4" :x-gap="14" :y-gap="12" responsive="screen">
              <n-form-item-gi label="名称">
                <n-input v-model:value="meal.name" placeholder="如 早餐、午餐、接待晚宴" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="时间">
                <n-date-picker
                  v-model:formatted-value="meal.time"
                  type="datetime"
                  format="yyyy:MM:dd:HH:mm"
                  value-format="yyyy:MM:dd:HH:mm"
                  placeholder="年:月:日:时:分"
                  clearable
                />
              </n-form-item-gi>
              <n-form-item-gi label="地点">
                <n-input v-model:value="meal.position" placeholder="请输入用餐地点" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="描述">
                <n-input v-model:value="meal.description" placeholder="餐标、桌号或其他说明" clearable />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </section>
      </n-space>
    </n-card>

    <n-flex justify="end" class="footer-actions">
      <n-button secondary @click="addMeal">
        <template #icon>
          <GoPlus />
        </template>
        添加用餐
      </n-button>
      <n-button type="primary" :loading="saving" @click="saveMeals">保存用餐</n-button>
    </n-flex>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash } from "vue-icons-plus/go";

const eventId = inject<Ref<string>>('eventId')
const eventApi = useEventApi()
const message = useMessage()

interface MealArrangement {
  id: string;
  backendId: number | null;
  name: string;
  time: string | null;
  position: string;
  description: string;
}

interface SaveStatus {
  type: "success" | "warning" | "error" | "info";
  message: string;
}

const idCounters = {
  meal: 1,
};

const meals = ref<MealArrangement[]>([]);
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

function createMeal(values: Partial<Omit<MealArrangement, "id">> = {}): MealArrangement {
  return {
    id: nextId("meal"),
    backendId: values.backendId ?? null,
    name: values.name ?? "",
    time: values.time ?? null,
    position: values.position ?? "",
    description: values.description ?? "",
  };
}

function addMeal() {
  meals.value.push(createMeal());
  saveStatus.value.message = "";
}

function removeMeal(index: number) {
  meals.value.splice(index, 1);
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

function moveMeal(index: number, offset: -1 | 1) {
  moveItem(meals.value, index, offset);
  saveStatus.value.message = "";
}

function getMealSummary(meal: MealArrangement) {
  const summaryParts = [meal.name, meal.time, meal.position].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写用餐信息";
}

interface MealPayload {
  id?: number;
  name: string;
  description: string;
  position: string;
  time: string | null;
}

function buildMealPayload(): MealPayload[] {
  return meals.value.map((meal) => ({
    ...(meal.backendId ? { id: meal.backendId } : {}),
    name: meal.name.trim(),
    description: meal.description.trim(),
    position: meal.position.trim(),
    time: toISO8601DateTime(meal.time),
  }));
}

function normalizeMealResponse(response: any): any[] {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.list)) return response.list;
  if (Array.isArray(response?.meals)) return response.meals;

  return [];
}

function formatMealTime(timeValue: any): string | null {
  if (!timeValue) return null

  const buildMealTime = (
    year: number,
    month: number,
    day: number,
    hours = 0,
    minutes = 0
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

    return `${year}:${String(month).padStart(2, '0')}:${String(day).padStart(2, '0')}:${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
  }

  const buildFromDate = (date: Date) => {
    if (Number.isNaN(date.getTime())) {
      return null
    }

    return buildMealTime(
      date.getFullYear(),
      date.getMonth() + 1,
      date.getDate(),
      date.getHours(),
      date.getMinutes()
    )
  }

  if (timeValue instanceof Date) {
    return buildFromDate(timeValue)
  }

  if (typeof timeValue === 'number') {
    return buildFromDate(new Date(timeValue))
  }

  if (typeof timeValue === 'string') {
    const value = timeValue.trim()
    if (!value) return null

    const timeMatch = value.match(
      /^(\d{4})[-/:](\d{2})[-/:](\d{2})(?:[ T:](\d{2}):(\d{2}))?/
    )

    if (timeMatch) {
      const [, year, month, day, hours = '00', minutes = '00'] = timeMatch
      return buildMealTime(
        Number(year),
        Number(month),
        Number(day),
        Number(hours),
        Number(minutes)
      )
    }

    return buildFromDate(new Date(value))
  }

  return null
}

const loadMealsData = async () => {
  if (!eventId?.value) return

  try {
    loading.value = true
    const response = await eventApi.getMealsByActivity(eventId.value)
    const loadedMeals = normalizeMealResponse(response)

    meals.value = loadedMeals.map((meal: any) => createMeal({
      backendId: Number.isFinite(Number(meal.id)) ? Number(meal.id) : null,
      name: meal.name || '',
      time: formatMealTime(meal.time),
      position: meal.position || '',
      description: meal.description || '',
    }))
    saveStatus.value.message = ''
  } catch (error: any) {
    message.error(error.message || '加载用餐数据失败')
    meals.value = []
  } finally {
    loading.value = false
  }
}

async function saveMeals() {
  if (!eventId?.value) {
    saveStatus.value = {
      type: "warning",
      message: "请先选择活动",
    };
    return;
  }

  saving.value = true;
  saveStatus.value = { type: "info", message: "" };

  try {
    const mealPayload = buildMealPayload();
    await eventApi.saveMeals(eventId.value, mealPayload)

    await loadMealsData()

    saveStatus.value = {
      type: "success",
      message: "用餐安排已保存",
    };
  } catch (error: any) {
    saveStatus.value = {
      type: "error",
      message: error.message || "用餐安排保存失败",
    };
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (eventId?.value) {
    loadMealsData()
  }
})

watch(() => eventId?.value, (newId) => {
  if (newId) {
    loadMealsData()
  }
})
</script>

<style scoped>
.dines-editor {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.status-alert {
  margin-bottom: 12px;
}

.meal-panel {
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid #eaecf0;
  border-radius: 8px;
  background: #fbfcfe;
}

.meal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.meal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.meal-order {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 14px;
  font-weight: 650;
}

.meal-summary {
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
  .meal-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .meal-title {
    width: 100%;
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .meal-summary {
    width: 100%;
    white-space: normal;
  }

  .footer-actions {
    justify-content: flex-start;
  }
}
</style>
