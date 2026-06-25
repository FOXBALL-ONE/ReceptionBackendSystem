<template>
  <div class="excel-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">Excel 导入</h1>
          <p class="page-subtitle">批量创建活动数据</p>
        </div>
      </div>
    </div>

    <Xls />
  </div>
</template>

<script setup lang="ts">
import Xls from "~/composables/event/xls.vue";
</script>

<style scoped>
.excel-page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.page-header {
  max-width: 1120px;
  margin: 0 auto 24px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  min-width: 0;
}

.page-title {
  margin: 0 0 4px;
  color: #1a1a1a;
  font-size: 28px;
  font-weight: 600;
}

.page-subtitle {
  margin: 0;
  color: #667085;
  font-size: 14px;
}

@media (max-width: 768px) {
  .excel-page {
    padding: 16px;
  }
}
</style>
