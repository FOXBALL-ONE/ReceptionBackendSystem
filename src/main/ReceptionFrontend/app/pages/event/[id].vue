<template>
  <n-space vertical :size="16" class="event-page">
    <n-card :bordered="false">
      <div class="edit-toolbar">
        <n-flex :size="10" wrap class="edit-tabs">
          <n-button
            v-for="tab in tabs"
            :key="tab.key"
            :type="activeTab === tab.key ? 'primary' : 'default'"
            :secondary="activeTab !== tab.key"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </n-button>
        </n-flex>
        <n-button
          class="export-btn"
          :loading="exporting"
          :disabled="!eventId"
          @click="handleExport"
        >
          <template #icon>
            <n-icon>
              <svg viewBox="0 0 24 24">
                <path fill="currentColor" d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
              </svg>
            </n-icon>
          </template>
          导出 Excel
        </n-button>
      </div>
    </n-card>

    <component :is="activeComponent" />
  </n-space>
</template>

<script setup lang="ts">
import AccessControl from "~/composables/event/accessControl.vue";
import Basic from "~/composables/event/basic.vue";
import Car from "~/composables/event/car.vue";
import Dines from "~/composables/event/dines.vue";
import InspectionPoints from "~/composables/event/lnspectionPoints.vue";
import Lodging from "~/composables/event/lodging.vue";
import OverviewOfTheCityAndCounty from "~/composables/event/overviewOfTheCityAndCounty.vue";
import Person from "~/composables/event/person.vue";
import Schedule from "~/composables/event/schedule.vue";
import WarmService from "~/composables/event/warmService.vue";
import Xls from "~/composables/event/xls.vue";

const route = useRoute();
const eventId = computed(() => route.params.id as string);

const eventApi = useEventApi();
const message = useMessage();
const exporting = ref(false);

const handleExport = async () => {
  if (!eventId.value) return;
  try {
    exporting.value = true;
    const ok = await eventApi.exportActivity(eventId.value);
    if (ok) message.success("导出成功");
  } catch (error: any) {
    message.error(error?.message || "导出失败");
  } finally {
    exporting.value = false;
  }
};

const tabs = [
  { key: "basic", label: "基本", component: Basic },
  { key: "accessControl", label: "访问控制", component: AccessControl },
  { key: "schedule", label: "日程", component: Schedule },
  { key: "person", label: "人员", component: Person },
  { key: "car", label: "乘车", component: Car },
  { key: "dines", label: "用餐", component: Dines },
  { key: "lodging", label: "住宿", component: Lodging },
  { key: "inspectionPoints", label: "考察点", component: InspectionPoints },
  { key: "warmService", label: "温馨服务", component: WarmService },
  { key: "overview", label: "市县概况", component: OverviewOfTheCityAndCounty },
  { key: "xls", label: "XLS导入", component: Xls },
] as const;

type TabKey = (typeof tabs)[number]["key"];

const activeTab = ref<TabKey>("basic");

const activeComponent = computed(() => tabs.find((tab) => tab.key === activeTab.value)?.component ?? Basic);

provide('eventId', eventId);

</script>

<style scoped>
.event-page {
  width: 100%;
}

.edit-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.edit-tabs {
  flex: 1;
  min-width: 0;
}

.export-btn {
  flex-shrink: 0;
}
</style>
