<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-mark">接</div>
        <h1 class="login-title">活动接待管理</h1>
        <p class="login-subtitle">请登录后进入后台管理</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <n-form-item label="账号" path="username">
          <n-input
            v-model:value="form.username"
            placeholder="请输入账号"
            clearable
            :input-props="{ autocomplete: 'username' }"
            @keydown.enter="handleSubmit"
          >
            <template #prefix>
              <n-icon>
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            clearable
            :input-props="{ autocomplete: 'current-password' }"
            @keydown.enter="handleSubmit"
          >
            <template #prefix>
              <n-icon>
                <svg viewBox="0 0 24 24">
                  <path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zM9 8V6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9z"/>
                </svg>
              </n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-button
          type="primary"
          size="large"
          block
          :loading="loading"
          class="login-submit"
          @click="handleSubmit"
        >
          登录
        </n-button>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {FormInst, FormRules} from "naive-ui";

definePageMeta({layout: "auth"});

const route = useRoute();
const router = useRouter();
const message = useMessage();
const auth = useAuthStore();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules: FormRules = {
  username: {required: true, message: "请输入账号", trigger: ["input", "blur"]},
  password: {required: true, message: "请输入密码", trigger: ["input", "blur"]},
};

const resolveRedirect = (): string => {
  const raw = route.query.redirect;
  if (typeof raw === "string" && raw.startsWith("/") && !raw.startsWith("//") && raw !== "/login") {
    return raw;
  }
  return "/";
};

const getErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  if (typeof error === "object" && error !== null && "statusMessage" in error) {
    const statusMessage = (error as {statusMessage?: unknown}).statusMessage;
    if (typeof statusMessage === "string" && statusMessage) {
      return statusMessage;
    }
  }
  return fallback;
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  try {
    loading.value = true;
    await auth.login(form.username.trim(), form.password);
    message.success("登录成功");
    await router.push(resolveRedirect());
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "登录失败"));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 32px 32px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  color: #ffffff;
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 10px 28px rgba(102, 126, 234, 0.35);
}

.login-title {
  margin: 0;
  font-size: 22px;
  font-weight: 650;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}

.login-subtitle {
  margin: 0;
  font-size: 13px;
  color: #8b92a7;
}

.login-submit {
  margin-top: 8px;
  border-radius: 10px;
  font-weight: 550;
}
</style>
