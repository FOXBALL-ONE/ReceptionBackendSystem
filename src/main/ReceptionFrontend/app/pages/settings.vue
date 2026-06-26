<template>
  <div class="settings-page">
    <div class="settings-header">
      <h1 class="page-title">账号设置</h1>
      <p class="page-subtitle">修改登录用户名、密码与两步验证</p>
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

      <!-- 两步验证（TOTP） -->
      <n-card class="settings-card totp-card" :bordered="false">
        <template #header>
          <div class="card-title">
            <n-icon size="20" class="card-icon">
              <svg viewBox="0 0 24 24">
                <path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
              </svg>
            </n-icon>
            <span>两步验证</span>
          </div>
        </template>

        <n-spin :show="totpLoading">
          <!-- 未开启：入口 -->
          <div v-if="totpPhase === 'off'" class="totp-section">
            <p class="totp-desc">开启后，登录时在密码之外还需输入动态码，显著提升账号安全。</p>
            <n-button type="primary" :loading="totpSetupLoading" @click="handleTotpSetup">
              开启两步验证
            </n-button>
          </div>

          <!-- 设置中：二维码 + 动态码确认 -->
          <div v-else-if="totpPhase === 'setup'" class="totp-section">
            <p class="totp-desc">用验证器（如 Microsoft/Google Authenticator）扫描下方二维码，然后输入动态码确认。</p>
            <div class="totp-qr">
              <img v-if="qrDataUrl" :src="qrDataUrl" alt="2FA QR" />
              <n-spin v-else />
            </div>
            <n-form-item label="无法扫码？手动输入此密钥" :show-feedback="false">
              <n-input-group>
                <n-input :value="setupSecret" readonly />
                <n-button @click="copyText(setupSecret)">复制</n-button>
              </n-input-group>
            </n-form-item>
            <n-form ref="totpEnableFormRef" :model="totpEnableForm" :rules="totpEnableRules" label-placement="top" size="medium">
              <n-form-item label="动态码" path="code">
                <n-input v-model:value="totpEnableForm.code" placeholder="请输入 6 位动态码" />
              </n-form-item>
              <n-flex justify="end">
                <n-button @click="cancelSetup">取消</n-button>
                <n-button type="primary" :loading="totpEnableLoading" @click="handleTotpEnable">
                  确认开启
                </n-button>
              </n-flex>
            </n-form>
          </div>

          <!-- 已开启 -->
          <div v-else class="totp-section">
            <n-alert v-if="backupCodes && backupCodes.length" type="warning" closable @close="backupCodes = null" class="backup-alert">
              <template #header>请妥善保存以下备用码（仅显示此次），丢失设备时可用于登录</template>
              <div class="backup-codes">
                <n-tag v-for="c in backupCodes" :key="c" size="large" class="backup-tag">{{ c }}</n-tag>
              </div>
              <n-button size="small" style="margin-top: 8px" @click="copyText(backupCodes.join('\n'))">复制全部</n-button>
            </n-alert>

            <template v-else>
              <p class="totp-desc totp-on">两步验证已开启，登录时需输入动态码。</p>
              <n-form ref="totpDisableFormRef" :model="totpDisableForm" :rules="totpDisableRules" label-placement="top" size="medium">
                <n-form-item label="当前密码（确认关闭）" path="password">
                  <n-input v-model:value="totpDisableForm.password" type="password" show-password-on="click" placeholder="请输入当前密码" />
                </n-form-item>
                <n-flex justify="end">
                  <n-button type="error" :loading="totpDisableLoading" @click="handleTotpDisable">
                    关闭两步验证
                  </n-button>
                </n-flex>
              </n-form>
            </template>
          </div>
        </n-spin>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {FormInst, FormRules} from "naive-ui";
import {toDataURL} from "qrcode";

const auth = useAuthStore();
const message = useMessage();

const usernameFormRef = ref<FormInst | null>(null);
const passwordFormRef = ref<FormInst | null>(null);
const totpEnableFormRef = ref<FormInst | null>(null);
const totpDisableFormRef = ref<FormInst | null>(null);

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

const copyText = async (text: string) => {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    message.success("已复制");
  } catch {
    message.warning("复制失败，请手动复制");
  }
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

// ===== 两步验证（TOTP）=====
const totpPhase = ref<"off" | "setup" | "enabled">("off");
const totpLoading = ref(false);
const totpSetupLoading = ref(false);
const totpEnableLoading = ref(false);
const totpDisableLoading = ref(false);
const setupSecret = ref("");
const qrDataUrl = ref("");
const backupCodes = ref<string[] | null>(null);
const totpEnableForm = reactive({code: ""});
const totpDisableForm = reactive({password: ""});

const totpEnableRules: FormRules = {
  code: {required: true, message: "请输入动态码", trigger: ["input", "blur"]},
};
const totpDisableRules: FormRules = {
  password: {required: true, message: "请输入当前密码", trigger: ["input", "blur"]},
};

const loadTotpStatus = async () => {
  try {
    totpLoading.value = true;
    const status = await auth.totpStatus();
    totpPhase.value = status.enabled ? "enabled" : "off";
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "获取两步验证状态失败"));
  } finally {
    totpLoading.value = false;
  }
};

const handleTotpSetup = async () => {
  try {
    totpSetupLoading.value = true;
    const view = await auth.totpSetup();
    setupSecret.value = view.secret;
    qrDataUrl.value = await toDataURL(view.otpauthUri, {width: 200, margin: 1});
    totpEnableForm.code = "";
    totpPhase.value = "setup";
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "获取两步验证二维码失败"));
  } finally {
    totpSetupLoading.value = false;
  }
};

const cancelSetup = () => {
  totpPhase.value = "off";
  setupSecret.value = "";
  qrDataUrl.value = "";
  totpEnableForm.code = "";
};

const handleTotpEnable = async () => {
  try {
    await totpEnableFormRef.value?.validate();
  } catch {
    return;
  }
  try {
    totpEnableLoading.value = true;
    backupCodes.value = await auth.totpEnable(totpEnableForm.code.trim());
    setupSecret.value = "";
    qrDataUrl.value = "";
    totpEnableForm.code = "";
    totpPhase.value = "enabled";
    message.success("两步验证已开启");
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "动态码错误，开启失败"));
  } finally {
    totpEnableLoading.value = false;
  }
};

const handleTotpDisable = async () => {
  try {
    await totpDisableFormRef.value?.validate();
  } catch {
    return;
  }
  try {
    totpDisableLoading.value = true;
    await auth.totpDisable(totpDisableForm.password);
    totpDisableForm.password = "";
    totpPhase.value = "off";
    message.success("两步验证已关闭");
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "关闭失败"));
  } finally {
    totpDisableLoading.value = false;
  }
};

onMounted(() => {
  loadTotpStatus();
});
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
  align-items: start;
}

.settings-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  background: #fff;
}

.totp-card {
  grid-column: 1 / -1;
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

.totp-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 460px;
}

.totp-desc {
  margin: 0;
  color: #555;
  font-size: 14px;
  line-height: 1.6;
}

.totp-desc.totp-on {
  color: #16a34a;
  font-weight: 550;
}

.totp-qr {
  display: flex;
  justify-content: center;
  padding: 12px;
  border: 1px solid #eef0f3;
  border-radius: 8px;
  background: #fafafa;
}

.totp-qr img {
  width: 200px;
  height: 200px;
}

.backup-alert {
  border-radius: 8px;
}

.backup-codes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.backup-tag {
  font-family: monospace;
  font-size: 15px;
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
