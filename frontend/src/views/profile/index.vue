<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-avatar">
        <el-avatar :size="80" :src="userInfo.avatar">
          <el-icon :size="40"><User /></el-icon>
        </el-avatar>
      </div>
      <div class="user-info">
        <h2 class="username">{{ userInfo.name || "未登录" }}</h2>
        <div class="user-meta">
          <div class="meta-item">
            <el-icon><Message /></el-icon>
            <span>{{ userInfo.email || "-" }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Phone /></el-icon>
            <span>{{ userInfo.mobile || "-" }}</span>
          </div>
          <div class="meta-item">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ userInfo.department || "-" }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Briefcase /></el-icon>
            <span>{{ userInfo.position || "-" }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalMeetings }}</div>
        <div class="stat-label">会议总数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalDuration }}</div>
        <div class="stat-label">总时长</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalTranscripts }}</div>
        <div class="stat-label">转写文档</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.storageUsed }}</div>
        <div class="stat-label">已用存储</div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card">
      <div class="menu-item" @click="handleMenuClick('notifications')">
        <div class="menu-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="menu-content">
          <div class="menu-title">通知设置</div>
          <div class="menu-desc">配置消息推送和提醒</div>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="handleMenuClick('security')">
        <div class="menu-icon">
          <el-icon><Lock /></el-icon>
        </div>
        <div class="menu-content">
          <div class="menu-title">安全设置</div>
          <div class="menu-desc">密码和安全选项</div>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="handleMenuClick('storage')">
        <div class="menu-icon">
          <el-icon><Folder /></el-icon>
        </div>
        <div class="menu-content">
          <div class="menu-title">存储管理</div>
          <div class="menu-desc">查看和管理存储空间</div>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="handleMenuClick('help')">
        <div class="menu-icon">
          <el-icon><QuestionFilled /></el-icon>
        </div>
        <div class="menu-content">
          <div class="menu-title">帮助中心</div>
          <div class="menu-desc">使用指南和常见问题</div>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="handleMenuClick('about')">
        <div class="menu-icon">
          <el-icon><InfoFilled /></el-icon>
        </div>
        <div class="menu-content">
          <div class="menu-title">关于我们</div>
          <div class="menu-desc">版本信息和联系方式</div>
        </div>
        <el-icon class="menu-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-card">
      <el-button type="danger" plain @click="handleLogout" style="width: 100%">
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "@/api";
import { userApi } from "@/api/user";

const router = useRouter();

const userInfo = ref({
  name: "",
  email: "",
  mobile: "",
  department: "",
  position: "",
  avatar: "",
});

const stats = ref({
  totalMeetings: 128,
  totalDuration: "256小时",
  totalTranscripts: 98,
  storageUsed: "12.5GB",
});

// 从后端获取用户信息
const loadUserInfo = async () => {
  const token = localStorage.getItem("token");
  if (!token) {
    ElMessage.warning("请先登录");
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
      userInfo.value = {
        name: user.name || "",
        email: user.email || "",
        mobile: user.mobile || "",
        department: user.department || "",
        position: user.position || "",
        avatar: user.avatar || "",
      };
    } else {
      ElMessage.error("获取用户信息失败");
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
    ElMessage.error("获取用户信息失败");
  }
};

const handleMenuClick = (type) => {
  ElMessage.info("功能开发中：" + type);
};

const handleLogout = () => {
  ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      userApi.logout();
      ElMessage.success("已退出登录");
      router.push("/login");
    })
    .catch(() => {
      // 取消退出
    });
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style lang="scss" scoped>
.profile-page {
  .user-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;
    display: flex;
    gap: 24px;

    .user-avatar {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .change-avatar {
        font-size: 12px;
      }
    }

    .user-info {
      flex: 1;

      .username {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 16px 0;
      }

      .user-meta {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 8px;
          color: var(--text-color-secondary);
          font-size: 14px;
        }
      }
    }
  }

  .stats-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: var(--primary-color);
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: var(--text-color-secondary);
      }
    }
  }

  .menu-card {
    background: var(--white);
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 16px;

    .menu-item {
      display: flex;
      align-items: center;
      padding: 16px 24px;
      cursor: pointer;
      transition: background 0.3s;
      border-bottom: 1px solid var(--border-color);

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background: var(--bg-color);
      }

      .menu-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: var(--primary-color);
        color: var(--white);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;

        .el-icon {
          font-size: 20px;
        }
      }

      .menu-content {
        flex: 1;

        .menu-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .menu-desc {
          font-size: 12px;
          color: var(--text-color-secondary);
        }
      }

      .menu-arrow {
        color: var(--text-color-secondary);
      }
    }
  }

  .logout-card {
    background: var(--white);
    border-radius: 8px;
    padding: 16px;
  }

  @media screen and (max-width: 768px) {
    .user-card {
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 16px;

      .user-info {
        .user-meta {
          align-items: center;
        }
      }
    }

    .stats-card {
      grid-template-columns: repeat(2, 1fr);
      padding: 16px;

      .stat-item {
        .stat-value {
          font-size: 20px;
        }
      }
    }

    .menu-card {
      .menu-item {
        padding: 12px 16px;

        .menu-icon {
          width: 36px;
          height: 36px;

          .el-icon {
            font-size: 18px;
          }
        }

        .menu-content {
          .menu-title {
            font-size: 14px;
          }

          .menu-desc {
            font-size: 11px;
          }
        }
      }
    }
  }
}
</style>
