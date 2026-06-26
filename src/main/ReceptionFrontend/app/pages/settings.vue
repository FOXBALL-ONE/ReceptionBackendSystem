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
              <n-flex :size="8" style="margin-top: 8px">
                <n-button size="small" @click="copyText(backupCodes.join('\n'))">复制全部</n-button>
                <n-button size="small" @click="downloadBackupCodes">下载 txt</n-button>
              </n-flex>
            </n-alert>

            <template v-else>
              <p class="totp-desc totp-on">两步验证已开启，登录时需输入动态码。</p>

              <!-- 备用码剩余/已用统计 -->
              <div class="backup-stat">
                <span>
                  备用码剩余 <b>{{ backupRemaining }}</b> / {{ backupTotal }}（已使用 {{ backupUsed }}）
                </span>
                <n-text v-if="backupRemaining === 0" type="warning" class="backup-exhausted">已用尽，建议重置</n-text>
              </div>

              <!-- 重置备用码 -->
              <n-form ref="totpResetFormRef" :model="totpResetForm" :rules="totpResetRules" label-placement="top" size="medium">
                <n-form-item label="当前密码（重置备用码，旧备用码全部失效）" path="password">
                  <n-input v-model:value="totpResetForm.password" type="password" show-password-on="click" placeholder="请输入当前密码" />
                </n-form-item>
                <n-flex justify="end">
                  <n-button :loading="totpResetLoading" @click="handleTotpResetBackup">重置备用码</n-button>
                </n-flex>
              </n-form>

              <n-divider style="margin: 8px 0" />

              <!-- 关闭两步验证 -->
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
  // 走 clipboard 工具：Clipboard API 在非安全上下文（HTTP）下不可用，需 execCommand 兜底
  const ok = await copyToClipboard(text);
  if (ok) {
    message.success("已复制");
  } else {
    message.warning("复制失败，请手动复制");
  }
};

/** 用于下载文件名的时间戳（YYYYMMDD-HHmm）。 */
const formatTimestamp = (date: Date): string => {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}-${pad(date.getHours())}${pad(date.getMinutes())}`;
};

/** 用于文档内展示的本地时间（YYYY-MM-DD HH:mm）。 */
const formatDisplayTime = (date: Date): string => {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
// 备用码剩余/已用数量（开启或重置后本地同步，刷新后由 totpStatus 回填）
const backupRemaining = ref(0);
const backupUsed = ref(0);
const backupTotal = computed(() => backupRemaining.value + backupUsed.value);
const totpEnableForm = reactive({code: ""});
const totpDisableForm = reactive({password: ""});
const totpResetFormRef = ref<FormInst | null>(null);
const totpResetForm = reactive({password: ""});
const totpResetLoading = ref(false);

const totpEnableRules: FormRules = {
  code: {required: true, message: "请输入动态码", trigger: ["input", "blur"]},
};
const totpDisableRules: FormRules = {
  password: {required: true, message: "请输入当前密码", trigger: ["input", "blur"]},
};
const totpResetRules: FormRules = {
  password: {required: true, message: "请输入当前密码", trigger: ["input", "blur"]},
};

const loadTotpStatus = async () => {
  try {
    totpLoading.value = true;
    const status = await auth.totpStatus();
    totpPhase.value = status.enabled ? "enabled" : "off";
    backupRemaining.value = status.backupRemaining;
    backupUsed.value = status.backupUsed;
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
    backupRemaining.value = backupCodes.value.length;
    backupUsed.value = 0;
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

/** 将一次性备用码导出为 txt 文档下载，便于离线妥善保存。 */
const downloadBackupCodes = () => {
  const codes = backupCodes.value;
  if (!codes || !codes.length) return;

  const username = auth.user?.username ?? "账号";
  const generatedAt = new Date();
  const stamp = formatTimestamp(generatedAt);

  const content = [
    "接待管理系统 — 两步验证备用恢复码",
    "========================================",
    "",
    `账号：${username}`,
    `生成时间：${formatDisplayTime(generatedAt)}`,
    "",
    "备用码（每枚仅可使用一次）：",
    ...codes.map((code) => `  ${code}`),
    "",
    "========================================",
    "重要提示：",
    "- 每枚备用码只能使用一次，使用后即作废。",
    "- 请妥善保管此文件，切勿泄露给他人。",
    '- 丢失验证设备时，可在两步登录页选择「使用备用码」登录。',
    "- 这些备用码仅本次显示，关闭两步验证后无法再次查看。",
  ].join("\n");

  downloadTextFile(`备用恢复码-${username}-${stamp}.txt`, content);
};

/** 手动重置备用码：校验当前密码后向服务端换发新的一组 10 枚备用码，旧备用码全部失效。 */
const handleTotpResetBackup = async () => {
  try {
    await totpResetFormRef.value?.validate();
  } catch {
    return;
  }
  try {
    totpResetLoading.value = true;
    const codes = await auth.totpResetBackup(totpResetForm.password);
    backupCodes.value = codes;
    backupRemaining.value = codes.length;
    backupUsed.value = 0;
    totpResetForm.password = "";
    totpResetFormRef.value?.restoreValidation();
    message.success("备用码已重置，请妥善保存");
  } catch (error: unknown) {
    message.error(getErrorMessage(error, "重置备用码失败"));
  } finally {
    totpResetLoading.value = false;
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

.backup-stat {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f5f7fa;
  font-size: 14px;
  color: #555;
}

.backup-stat b {
  color: #6366f1;
}

.backup-exhausted {
  font-size: 13px;
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
