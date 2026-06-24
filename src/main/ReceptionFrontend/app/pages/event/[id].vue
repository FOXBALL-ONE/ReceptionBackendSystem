<template>
  <n-space vertical :size="16" class="event-page">
    <n-card :bordered="false">
      <n-flex :size="10" wrap>
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
</style>
