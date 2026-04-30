<template>
  <div class="layout-container">
    <!-- PC端侧边栏 -->
    <aside v-if="!isMobile" class="sidebar">
      <div class="logo">
        <el-icon><Microphone /></el-icon>
        <span>会议录音</span>
      </div>
      <el-menu :default-active="activeMenu" class="sidebar-menu" router>
        <el-menu-item index="/meetings">
          <el-icon><Document /></el-icon>
          <span>会议记录</span>
        </el-menu-item>
        <el-menu-item index="/upload">
          <el-icon><Upload /></el-icon>
          <span>上传录音</span>
        </el-menu-item>
        <el-menu-item index="/device">
          <el-icon><Microphone /></el-icon>
          <span>设备管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- PC端顶部栏 -->
      <header v-if="!isMobile" class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-user">
          <el-avatar :size="32" :src="userAvatar"></el-avatar>
          <span class="username">{{ userName }}</span>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>

      <!-- 移动端底部导航 -->
      <nav v-if="isMobile" class="mobile-nav">
        <div
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
          @click="handleNavClick(item.path)"
        >
          <el-icon :size="24">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </div>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import api from "@/api";

const route = useRoute();
const router = useRouter();

const isMobile = ref(false);
const userName = ref("未登录");
const userAvatar = ref("");

const navItems = [
  { path: "/meetings", title: "会议记录", icon: "Document" },
  { path: "/upload", title: "上传录音", icon: "Upload" },
  { path: "/device", title: "设备管理", icon: "Microphone" },
  { path: "/profile", title: "个人中心", icon: "User" },
];

const activeMenu = computed(() => {
  return route.path;
});

const pageTitle = computed(() => {
  return route.meta.title || "会议录音转文字";
});

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
};

const handleNavClick = (path) => {
  router.push(path);
};

// 获取当前用户信息
const loadUserInfo = async () => {
  const token = localStorage.getItem("token");
  if (!token) {
    userName.value = "未登录";
    return;
  }

  try {
    const response = await api.get("/auth/user/info", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.code === 200) {
      const user = response.data;
      userName.value = user.name || "未登录";
      userAvatar.value = user.avatar || "";

      // 保存到localStorage
      localStorage.setItem("userInfo", JSON.stringify(user));
    } else {
      userName.value = "未登录";
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
    userName.value = "未登录";
  }
};

onMounted(() => {
  checkMobile();
  window.addEventListener("resize", checkMobile);
  loadUserInfo();
});

onUnmounted(() => {
  window.removeEventListener("resize", checkMobile);
});
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100vh;
  background: var(--bg-color);
}

.sidebar {
  width: 240px;
  background: var(--white);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 600;
    color: var(--primary-color);
    border-bottom: 1px solid var(--border-color);

    .el-icon {
      margin-right: 8px;
      font-size: 24px;
    }
  }

  .sidebar-menu {
    border: none;
    flex: 1;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: var(--white);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;

  .header-title {
    font-size: 18px;
    font-weight: 600;
  }

  .header-user {
    display: flex;
    align-items: center;

    .username {
      margin-left: 8px;
      font-size: 14px;
    }
  }
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  @media screen and (max-width: 768px) {
    padding: 16px;
    padding-bottom: 70px;
  }
}

.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--white);
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    height: 100%;
    color: var(--text-color-secondary);
    cursor: pointer;
    transition: color 0.3s;

    &.active {
      color: var(--primary-color);
    }

    span {
      font-size: 12px;
      margin-top: 4px;
    }
  }
}
</style>
