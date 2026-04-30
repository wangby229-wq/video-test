<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <el-icon :size="48" color="#1890ff"><Microphone /></el-icon>
        <h1>会议录音转文字系统</h1>
        <p>智能语音识别，高效会议记录</p>
      </div>

      <!-- 企业微信内打开 -->
      <div v-if="isInWechat" class="wechat-login">
        <el-button
          type="primary"
          size="large"
          style="width: 100%"
          :loading="loading"
          @click="handleWechatLogin"
        >
          <el-icon><User /></el-icon>
          企业微信授权登录
        </el-button>
      </div>

      <!-- 外部浏览器打开 -->
      <div v-else class="qr-login">
        <div class="qr-tip">请使用企业微信扫码登录</div>
        <div class="qr-container" v-loading="loading">
          <el-button
            v-if="qrUrl"
            type="primary"
            size="large"
            style="width: 100%"
            @click="handleQrLogin"
          >
            <el-icon><View /></el-icon>
            打开企业微信扫码
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { authApi } from "@/api/auth";
import { config } from "@/config";

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const isInWechat = ref(false);
const qrUrl = ref("");

const checkWechatEnv = () => {
  isInWechat.value = authApi.isInWechat();
};

const handleWechatLogin = async () => {
  loading.value = true;
  try {
    // 获取企业微信OAuth URL
    const redirectUri = encodeURIComponent(config.redirectUri);
    const response = await authApi.getOAuthUrl(redirectUri);

    if (response.code === 200) {
      // 跳转到企业微信授权页面
      window.location.href = response.data.oauthUrl;
    } else {
      ElMessage.error(response.message || "获取授权链接失败");
    }
  } catch (error) {
    ElMessage.error("获取授权链接失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const loadQrUrl = async () => {
  if (isInWechat.value) return;

  loading.value = true;
  try {
    const redirectUri = encodeURIComponent(config.redirectUri);
    const response = await authApi.getQrConnectUrl(redirectUri);

    if (response.code === 200) {
      qrUrl.value = response.data.qrUrl;
    } else {
      ElMessage.error(response.message || "获取二维码失败");
    }
  } catch (error) {
    ElMessage.error("获取二维码失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const handleQrLogin = () => {
  if (qrUrl.value) {
    window.location.href = qrUrl.value;
  }
};

const handleCallback = async (code) => {
  loading.value = true;
  try {
    const response = await authApi.wechatCallback(code);

    if (response.code === 200) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("userInfo", JSON.stringify(response.data.user));

      ElMessage.success("登录成功");
      router.push("/meetings");
    } else {
      ElMessage.error(response.message || "登录失败");
    }
  } catch (error) {
    ElMessage.error("登录失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const handleWechatSilentLogin = async (code) => {
  loading.value = true;
  try {
    const response = await authApi.wechatSilentLogin(code);

    if (response.code === 200) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("userInfo", JSON.stringify(response.data.user));
      ElMessage.success("登录成功");
      router.push("/meetings");
    } else {
      ElMessage.error(response.message || "登录失败");
      loadQrUrl();
    }
  } catch (error) {
    ElMessage.error("登录失败：" + error.message);
    loadQrUrl();
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  checkWechatEnv();

  // 检查URL中的token和用户信息（从企业微信OAuth重定向返回）
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  const userStr = urlParams.get("user");
  const error = urlParams.get("error");
  const code = urlParams.get("code");

  // 处理错误
  if (error) {
    ElMessage.error("登录失败：" + decodeURIComponent(error));
    if (isInWechat.value) {
      // 企业微信内失败，静默重新授权
      autoWechatAuth();
    } else {
      loadQrUrl();
    }
    return;
  }

  // 处理从企业微信OAuth重定向返回的token和user
  if (token && userStr) {
    try {
      const user = JSON.parse(decodeURIComponent(userStr));
      localStorage.setItem("token", token);
      localStorage.setItem("userInfo", JSON.stringify(user));
      ElMessage.success("登录成功");
      router.push("/meetings");
      return;
    } catch (e) {
      console.error("解析用户信息失败:", e);
      ElMessage.error("登录失败：解析用户信息失败");
    }
  }

  // 如果已经登录，直接跳转
  const localToken = localStorage.getItem("token");
  if (localToken) {
    router.push("/meetings");
    return;
  }

  // 判断是企业微信内还是外部浏览器
  if (isInWechat.value) {
    // 企业微信内：静默自动登录
    if (code) {
      // 有code，说明是企业微信授权回调
      handleWechatSilentLogin(code);
    } else {
      // 没有code，需要先静默授权
      autoWechatAuth();
    }
  } else {
    // 外部浏览器：显示二维码扫码登录
    loadQrUrl();
  }
});

// 企业微信静默授权
const autoWechatAuth = async () => {
  try {
    const redirectUri = encodeURIComponent(config.redirectUri);
    const response = await authApi.getOAuthUrl(redirectUri);

    if (response.code === 200) {
      window.location.href = response.data.oauthUrl;
    } else {
      ElMessage.error(response.message || "获取授权链接失败");
      loading.value = false;
    }
  } catch (error) {
    ElMessage.error("获取授权链接失败：" + error.message);
    loading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .login-container {
    width: 400px;
    max-width: 90%;
    background: var(--white);
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);

    @media screen and (max-width: 768px) {
      padding: 24px;
    }
  }

  .login-header {
    text-align: center;
    margin-bottom: 32px;

    h1 {
      font-size: 24px;
      font-weight: 600;
      margin: 16px 0 8px;
      color: var(--text-color);
    }

    p {
      font-size: 14px;
      color: var(--text-color-secondary);
      margin: 0;
    }
  }

  .wechat-login {
    .el-button {
      height: 48px;
      font-size: 16px;
    }
  }

  .qr-login {
    .qr-tip {
      text-align: center;
      font-size: 14px;
      color: var(--text-color-secondary);
      margin-bottom: 16px;
    }

    .qr-container {
      width: 100%;
      min-height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-color);
      border-radius: 8px;

      img {
        max-width: 100%;
        max-height: 300px;
      }
    }
  }
}
</style>
