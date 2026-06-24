<template>
  <n-space vertical :size="16">
    <n-card title="工作人员" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addStaffGroup">
          <template #icon>
            <GoPlus />
          </template>
          添加分组
        </n-button>
      </template>

      <n-empty v-if="staffGroups.length === 0" description="暂无工作人员">
        <template #extra>
          <n-button type="primary" @click="addStaffGroup">添加分组</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="14">
        <section v-for="(group, groupIndex) in staffGroups" :key="group.id" class="item-panel">
          <div class="panel-header">
            <div class="panel-title">
              <span class="panel-order">分组 {{ groupIndex + 1 }}</span>
              <span class="panel-summary">{{ getStaffGroupSummary(group) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="groupIndex === 0"
                aria-label="上移工作人员分组"
                @click="moveStaffGroup(groupIndex, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="groupIndex === staffGroups.length - 1"
                aria-label="下移工作人员分组"
                @click="moveStaffGroup(groupIndex, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                type="error"
                aria-label="删除工作人员分组"
                @click="removeStaffGroup(groupIndex)"
              >
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1" responsive="screen">
              <n-form-item-gi label="分组名称">
                <n-input v-model:value="group.name" placeholder="请输入分组名称" clearable />
              </n-form-item-gi>
            </n-grid>
          </n-form>

          <div class="section-heading">
            <span>成员</span>
            <n-button size="small" secondary type="primary" @click="addStaffMember(group)">
              <template #icon>
                <GoPlus />
              </template>
              添加成员
            </n-button>
          </div>

          <n-empty v-if="group.members.length === 0" description="暂无成员" size="small" />

          <n-space v-else vertical :size="8">
            <div v-for="(member, memberIndex) in group.members" :key="member.id" class="member-row">
              <n-input v-model:value="member.name" placeholder="姓名" clearable />
              <n-input v-model:value="member.role" placeholder="职责" clearable />
              <n-input v-model:value="member.phone" placeholder="电话" clearable />
              <n-button
                circle
                quaternary
                type="error"
                aria-label="删除成员"
                @click="removeStaffMember(group, memberIndex)"
              >
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </div>
          </n-space>
        </section>
      </n-space>
    </n-card>

    <n-card title="温馨提示" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="addWarmTip">
          <template #icon>
            <GoPlus />
          </template>
          添加提示
        </n-button>
      </template>

      <n-empty v-if="warmTips.length === 0" description="暂无提示">
        <template #extra>
          <n-button type="primary" @click="addWarmTip">添加提示</n-button>
        </template>
      </n-empty>

      <n-space v-else vertical :size="14">
        <section v-for="(tip, tipIndex) in warmTips" :key="tip.id" class="item-panel">
          <div class="panel-header">
            <div class="panel-title">
              <span class="panel-order">提示 {{ tipIndex + 1 }}</span>
              <span class="panel-summary">{{ getWarmTipSummary(tip) }}</span>
            </div>
            <n-flex :size="4" align="center">
              <n-button
                circle
                quaternary
                :disabled="tipIndex === 0"
                aria-label="上移温馨提示"
                @click="moveWarmTip(tipIndex, -1)"
              >
                <template #icon>
                  <GoArrowUp />
                </template>
              </n-button>
              <n-button
                circle
                quaternary
                :disabled="tipIndex === warmTips.length - 1"
                aria-label="下移温馨提示"
                @click="moveWarmTip(tipIndex, 1)"
              >
                <template #icon>
                  <GoArrowDown />
                </template>
              </n-button>
              <n-button circle quaternary type="error" aria-label="删除温馨提示" @click="removeWarmTip(tipIndex)">
                <template #icon>
                  <GoTrash />
                </template>
              </n-button>
            </n-flex>
          </div>

          <n-form label-placement="top" :show-feedback="false">
            <n-grid cols="1" :y-gap="12" responsive="screen">
              <n-form-item-gi label="提示标题">
                <n-input v-model:value="tip.title" placeholder="请输入提示标题" clearable />
              </n-form-item-gi>
              <n-form-item-gi label="提示内容">
                <n-input
                  v-model:value="tip.content"
                  type="textarea"
                  placeholder="请输入提示内容"
                  :autosize="{ minRows: 4, maxRows: 10 }"
                  clearable
                />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </section>
      </n-space>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { GoArrowDown, GoArrowUp, GoPlus, GoTrash } from "vue-icons-plus/go";

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

defineProps<{
  staffGroups: StaffGroup[];
  warmTips: WarmTip[];
  addStaffGroup: () => void;
  removeStaffGroup: (index: number) => void;
  moveStaffGroup: (index: number, offset: -1 | 1) => void;
  addStaffMember: (group: StaffGroup) => void;
  removeStaffMember: (group: StaffGroup, index: number) => void;
  addWarmTip: () => void;
  removeWarmTip: (index: number) => void;
  moveWarmTip: (index: number, offset: -1 | 1) => void;
}>();

function getStaffGroupSummary(group: StaffGroup) {
  const memberCount = group.members.length > 0 ? `${group.members.length} 人` : "";
  const summaryParts = [group.name, memberCount].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写分组信息";
}

function getWarmTipSummary(tip: WarmTip) {
  const summaryParts = [tip.title, tip.content].filter(Boolean);

  return summaryParts.length > 0 ? summaryParts.join(" / ") : "未填写提示内容";
}
</script>

<style scoped>
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

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #344054;
  font-size: 14px;
  font-weight: 650;
}

.member-row {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(160px, 1fr) minmax(150px, 0.9fr) auto;
  gap: 10px;
  align-items: center;
}

@media (max-width: 720px) {
  .panel-header,
  .panel-title,
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .panel-title,
  .panel-summary {
    width: 100%;
  }

  .panel-summary {
    white-space: normal;
  }

  .member-row {
    grid-template-columns: 1fr;
  }
}
</style>
