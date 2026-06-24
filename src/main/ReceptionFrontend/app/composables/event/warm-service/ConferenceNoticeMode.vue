<template>
  <n-card title="参会须知" :bordered="false">
    <n-form label-placement="top" :show-feedback="false">
      <n-grid cols="1" :y-gap="12" responsive="screen">
        <n-form-item-gi label="标题栏">
          <n-input v-model:value="conferenceNotice.title" placeholder="请输入标题" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="内容栏">
          <n-input
            v-model:value="conferenceNotice.content"
            type="textarea"
            placeholder="请输入参会须知内容"
            :autosize="{ minRows: 8, maxRows: 16 }"
            clearable
          />
        </n-form-item-gi>
      </n-grid>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
interface ConferenceNotice {
  title: string;
  content: string;
}

defineProps<{
  conferenceNotice: ConferenceNotice;
}>();
</script>
