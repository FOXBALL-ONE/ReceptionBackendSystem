<template>
  <div class="settings-page">
    <div class="settings-header">
      <h1 class="page-title">账号设置</h1>
      <p class="page-subtitle">修改登录用户名与密码</p>
    </div>

    <div class="settings-grid">
      <!-- 修改用户名 -->
      <n-card class="settings-card" :bordered="false">
        <template #header>
          <div class="card-title">
            <n-icon size="20" class="card-icon">
              <svg viewBox="0 0 24 24">
                <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </n-icon>
            <span>修改用户名</span>
          </div>
        </template>
        <n-form
          ref="usernameFormRef"
          :model="usernameForm"
          :rules="usernameRules"
          label-placement="top"
          size="medium"
        >
          <n-form-item label="当前用户名">
            <n-input :value="auth.user?.username ?? ''" disabled />
          </n-form-item>
          <n-form-item label="新用户名" path="newUsername">
            <n-input
              v-model:value="usernameForm.newUsername"
              placeholder="请输入新用户名"
              maxlength="64"
              clearable
            />
          </n-form-item>
          <n-form-item label="当前密码（确认身份）" path="password">
            <n-input
              v-model:value="usernameForm.password"
              type="password"
              show-password-on="click"
              placeholder="请输入当前密码"
            />
          </n-form-item>
          <n-flex justify="end">
            <n-button type="primary" :loading="usernameSaving" @click="handleSubmitUsername">
              保存
            </n-button>
          </n-flex>
        </n-form>
      </n-card>

      <!-- 修改密码 -->
      <n-card class="settings-card" :bordered="false">
        <template #header>
          <div class="card-title">
            <n-icon size="20" class="card-icon">
              <svg viewBox="0 0 24 24">
                <path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3-9H9V6c0-1.66 1.34-3 3-3s3 1.34 3 3v2z"/>
              </svg>
            </n-icon>
            <span>修改密码</span>
          </div>
        </template>
        <n-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-placement="top"
          size="medium"
        >
          <n-form-item label="原密码" path="oldPassword">
            <n-input
              v-model:value="passwordForm.oldPassword"
              type="password"
              show-password-on="click"
              placeholder="请输入原密码"
            />
          </n-form-item>
          <n-form-item label="新密码（至少 6 位）" path="newPassword">
            <n-input
              v-model:value="passwordForm.newPassword"
              type="password"
              show-password-on="click"
              placeholder="请输入新密码"
            />
          </n-form-item>
          <n-form-item label="确认新密码" path="confirmPassword">
            <n-input
              v-model:value="passwordForm.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="请再次输入新密码"
            />
          </n-form-item>
          <n-flex justify="end">
            <n-button type="primary" :loading="passwordSaving" @click="handleSubmitPassword">
              保存
            </n-button>
          </n-flex>
        </n-form>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {FormInst, FormRules} from "naive-ui";

const auth = useAuthStore();
const message = useMessage();

const usernameFormRef = ref<FormInst | null>(null);
const passwordFormRef = ref<FormInst | null>(null);
const usernameSaving = ref(false);
const passwordSaving = ref(false);

const usernameForm = reactive({newUsername: "", password: ""});
const passwordForm = reactive({oldPassword: "", newPassword: "", confirmPassword: ""});

const usernameRules: FormRules = {
  newUsername: {required: true, message: "请输入新用户名", trigger: ["input", "blur"]},
  password: {required: true, message: "请输入当前密码", trigger: ["input", "blur"]},
};

const passwordRules: FormRules = {
  oldPassword: {required: true, message: "请输入原密码", trigger: ["input", "blur"]},
  newPassword: [
    {required: true, message: "请输入新密码", trigger: ["input", "blur"]},
    {min: 6, message: "新密码至少 6 位", trigger: ["input", "blur"]},
  ],
  confirmPassword: {
    required: true,
    trigger: ["input", "blur"],
    validator: (_rule, value) => {
      if (!value) {
        return new Error("请再次输入新密码");
      }
      return value === passwordForm.newPassword ? true : new Error("两次输入的新密码不一致");
    },
  },
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

const handleSubmitUsername = async () => {
  try {
    await usernameFormRef.value?.validate();
  } catch {
    return;
  }

  try {
    usernameSaving.value = true;
    await auth.changeUsername(usernameForm.newUsername.trim(), usernameForm.password);
    message.success("用户名修改成功");
    usernameForm.newUsername = "";
    usernameForm.password = "";
    usernameFormRef.value?.restoreValidation();
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "用户名修改失败"));
  } finally {
    usernameSaving.value = false;
  }
};

const handleSubmitPassword = async () => {
  try {
    await passwordFormRef.value?.validate();
  } catch {
    return;
  }

  try {
    passwordSaving.value = true;
    await auth.changePassword(passwordForm.oldPassword, passwordForm.newPassword);
    message.success("密码修改成功");
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
    passwordFormRef.value?.restoreValidation();
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "密码修改失败"));
  } finally {
    passwordSaving.value = false;
  }
};
</script>

<style scoped>
.settings-page {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.settings-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 20px;
}

.settings-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  background: #fff;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-icon {
  color: #6366f1;
}

@media (max-width: 768px) {
  .settings-page {
    padding: 16px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
