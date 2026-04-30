<template>
  <div class="device-bind-page">
    <div class="bind-container">
      <div class="header">
        <h2>设备绑定</h2>
      </div>

      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>{{ loadingText }}</p>
      </div>

      <div v-else-if="error" class="error">
        <el-icon><WarningFilled /></el-icon>
        <p>{{ error }}</p>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>

      <div v-else-if="bindSuccess" class="success">
        <div class="success-animation">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        </div>
        <h3>绑定成功</h3>
        <p class="device-name">设备 "{{ deviceInfo.deviceName }}"</p>
        <p class="bind-info-text">已成功绑定到您的账号</p>
        <div class="success-details">
          <div class="detail-item">
            <span class="detail-icon">📱</span>
            <span class="detail-label">设备序列号</span>
            <span class="detail-value">{{ deviceInfo.deviceSerial }}</span>
          </div>
        </div>
        <div class="success-actions">
          <el-button type="primary" size="large" @click="goToDeviceManage">
            返回设备管理
          </el-button>
          <el-button size="large" @click="goBack">完成</el-button>
        </div>
      </div>

      <div v-else-if="alreadyBound" class="already-bound">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <h3>设备已被绑定</h3>
        <div class="device-info">
          <p><strong>设备名称：</strong>{{ deviceInfo.deviceName }}</p>
          <p><strong>设备序列号：</strong>{{ deviceInfo.deviceSerial }}</p>
          <p><strong>绑定用户：</strong>{{ deviceInfo.userNames }}</p>
          <p>
            <strong>绑定时间：</strong>{{ formatTime(deviceInfo.bindTime) }}
          </p>
        </div>
        <p class="hint">如需重新绑定，请先解绑现有设备</p>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>

      <div v-else class="device-info-card">
        <div class="device-icon">
          <el-icon :size="60"><Microphone /></el-icon>
        </div>

        <h3>即将绑定以下设备</h3>

        <div class="info-list">
          <div class="info-item">
            <span class="label">设备名称</span>
            <span class="value">{{ deviceInfo.deviceName }}</span>
          </div>
          <div class="info-item">
            <span class="label">设备序列号</span>
            <span class="value">{{ deviceInfo.deviceSerial }}</span>
          </div>
          <div class="info-item">
            <span class="label">设备状态</span>
            <span class="value">
              <el-tag
                :type="deviceInfo.status === 'online' ? 'success' : 'info'"
                size="small"
              >
                {{ deviceInfo.status === "online" ? "在线" : "离线" }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="label">绑定用户</span>
            <span class="value">{{
              hasCode ? "（点击确认绑定后获取）" : "无"
            }}</span>
          </div>
        </div>

        <div v-if="!hasCode" class="bind-warning">
          <p>⚠️ 无法获取用户身份，请重新扫码</p>
        </div>

        <div class="bind-info">
          <p>绑定后，该设备的录音将自动推送到您的企业微信</p>
        </div>

        <div class="actions">
          <el-button
            @click="handleBind"
            :loading="binding"
            :disabled="!hasCode"
            type="button"
            size="large"
            class="bind-button"
          >
            确认绑定
          </el-button>
          <el-button size="large" @click="goBack" class="cancel-button">
            取消
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  Loading,
  WarningFilled,
  CircleCheckFilled,
  Microphone,
} from "@element-plus/icons-vue";
import api from "@/api/index";

const loading = ref(true);
const loadingText = ref("加载中...");
const binding = ref(false);
const error = ref("");
const bindSuccess = ref(false);
const alreadyBound = ref(false);
const serial = ref("");
const code = ref("");

const deviceInfo = ref({
  deviceSerial: "",
  deviceName: "",
  deviceId: "",
  status: "",
  isBound: false,
  userNames: "",
});

const hasCode = computed(() => !!code.value);

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  serial.value = urlParams.get("serial") || "";
  code.value = urlParams.get("code") || "";

  if (!serial.value) {
    error.value = "设备序列号无效";
    loading.value = false;
    return;
  }

  if (!code.value) {
    loadingText.value = "正在跳转到企业微信授权...";
    getOAuthUrl();
    return;
  }

  loadingText.value = "正在获取设备信息...";
  loadDeviceInfo();
});

const getOAuthUrl = async () => {
  try {
    const response = await api.get("/devices/oauth-url", {
      params: { serial: serial.value },
    });

    if (response.code === 200 && response.data.oauthUrl) {
      window.location.href = response.data.oauthUrl;
    } else {
      error.value = "获取授权信息失败";
      loading.value = false;
    }
  } catch (e) {
    error.value = "获取授权信息失败，请重试";
    loading.value = false;
    console.error(e);
  }
};

const loadDeviceInfo = async () => {
  try {
    const response = await api.get("/devices/qrcode-info", {
      params: { serial: serial.value },
    });

    if (response.code === 200) {
      deviceInfo.value = response.data;
      if (response.data.isBound) {
        alreadyBound.value = true;
      }
    } else {
      error.value = response.message || "获取设备信息失败";
    }
  } catch (e) {
    error.value = "加载设备信息失败，请检查网络";
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleBind = async () => {
  if (!code.value) {
    ElMessage.error("用户身份验证失败，请重新扫码");
    return;
  }

  binding.value = true;

  try {
    const response = await api.post("/devices/qrcode-bind", null, {
      params: { serial: serial.value, code: code.value },
    });

    if (response.code === 200) {
      bindSuccess.value = true;
    } else {
      ElMessage.error(response.message || "绑定失败");
    }
  } catch (e) {
    ElMessage.error("绑定失败，请重试");
    console.error(e);
  } finally {
    binding.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return "-";
  const date = new Date(time);
  return date.toLocaleString("zh-CN");
};

const goBack = () => {
  window.location.href = "/meeting/device";
};

const goToDeviceManage = () => {
  window.location.href = "/meeting/device";
};
</script>

<style scoped>
.device-bind-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.bind-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 420px;
  overflow: hidden;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  text-align: center;
}

.header h2 {
  margin: 0;
  font-size: 20px;
}

.loading,
.error,
.success,
.already-bound {
  padding: 60px 30px;
  text-align: center;
}

.loading {
  color: #666;
}

.loading .el-icon {
  font-size: 40px;
  margin-bottom: 16px;
  color: #667eea;
}

.error {
  color: #f56c6c;
}

.error .el-icon {
  font-size: 50px;
  margin-bottom: 16px;
}

.error p {
  color: #606266;
  font-size: 14px;
  margin: 16px 0 24px;
}

.error .el-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  padding: 0 20px;
  margin: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border-width: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.success {
  padding: 40px 24px;
}

.success-animation {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 24px rgba(103, 194, 58, 0.3);
  animation: success-pop 0.5s ease-out;
}

@keyframes success-pop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  font-size: 48px;
  color: white;
}

.success h3 {
  font-size: 24px;
  margin: 0 0 8px 0;
  color: #333;
}

.device-name {
  font-size: 16px;
  color: #409eff;
  margin: 8px 0;
  font-weight: 500;
}

.bind-info-text {
  font-size: 14px;
  color: #909399;
  margin: 8px 0 24px;
}

.success-details {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.detail-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
  text-align: center;
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.success-actions .el-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  padding: 0 20px;
  margin: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border-width: 0;
}

.success-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.success-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a70d9 0%, #6a4190 100%);
}

.success-actions .el-button:not(.el-button--primary) {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-width: 1px;
}

.success-actions .el-button:not(.el-button--primary):hover {
  background: #e4e7ed;
  border-color: #c0c4cc;
}

.warning-icon {
  font-size: 50px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.already-bound {
  padding: 40px 24px;
}

.already-bound h3 {
  font-size: 20px;
  margin: 0 0 20px 0;
  color: #333;
}

.device-info {
  background: linear-gradient(135deg, #fdf6ec 0%, #fef0e6 100%);
  border-radius: 12px;
  padding: 20px;
  margin: 16px 0;
  text-align: left;
}

.device-info p {
  margin: 10px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.device-info strong {
  color: #303133;
  font-weight: 600;
}

.hint {
  color: #909399;
  font-size: 13px;
  margin: 16px 0 24px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.already-bound .el-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  padding: 0 20px;
  margin: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border-width: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.bind-warning {
  background: #fdf6ec;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  text-align: center;
}

.bind-warning p {
  margin: 0;
  color: #e6a23c;
  font-size: 13px;
}

.device-info-card {
  padding: 24px;
}

.device-icon {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 20px;
  color: white;
}

.device-icon .el-icon {
  opacity: 0.9;
}

.device-info-card h3 {
  text-align: center;
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.info-list {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e8e8e8;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.bind-info {
  text-align: center;
  padding: 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.bind-info p {
  margin: 0;
  color: #409eff;
  font-size: 13px;
  line-height: 1.6;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}

.actions .el-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  padding: 0 20px;
  margin: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border-width: 0;
}

.actions .bind-button {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.4);
}

.actions .bind-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #5daf34 0%, #77c156 100%);
}

.actions .bind-button:disabled {
  background: #c0c4cc;
  box-shadow: none;
}

.actions .cancel-button {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-width: 1px;
}

.actions .cancel-button:hover {
  background: #e4e7ed;
  border-color: #c0c4cc;
}
</style>
