<template>
  <div class="meetings-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会议记录"
        prefix-icon="Search"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="filterStatus"
        placeholder="状态筛选"
        clearable
        @change="handleFilter"
      >
        <el-option label="全部" value="" />
        <el-option label="转写中" value="processing" />
        <el-option label="已完成" value="completed" />
        <el-option label="失败" value="failed" />
      </el-select>
    </div>

    <!-- 会议列表 -->
    <div class="meeting-list" v-loading="loading">
      <div
        v-for="meeting in meetings"
        :key="meeting.id"
        class="meeting-card"
        @click="handleMeetingClick(meeting)"
      >
        <div class="meeting-header">
          <div class="meeting-title">{{ meeting.title }}</div>
          <el-tag :type="getStatusType(meeting.status)" size="small">
            {{ getStatusText(meeting.status) }}
          </el-tag>
        </div>
        <div class="meeting-info">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ meeting.date }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ meeting.duration }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ meeting.participants }}人</span>
          </div>
        </div>
        <div class="meeting-content" v-html="meeting.summary"></div>
        <div class="meeting-footer">
          <div class="meeting-tags">
            <el-tag
              v-for="tag in meeting.tags"
              :key="tag"
              size="small"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="meeting-actions">
            <el-button text size="small" @click.stop="handleDownload(meeting)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button text size="small" @click.stop="handleShare(meeting)">
              <el-icon><Share /></el-icon>
              分享
            </el-button>
            <el-button
              text
              size="small"
              type="danger"
              @click.stop="handleDelete(meeting)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="meetings.length === 0 && !loading"
        description="暂无会议记录"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { meetingApi } from "@/api/meeting";

const router = useRouter();

const searchKeyword = ref("");
const filterStatus = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const meetings = ref([]);

const fetchMeetings = async () => {
  loading.value = true;
  try {
    const response = await meetingApi.getList({
      keyword: searchKeyword.value,
      status: filterStatus.value,
      page: currentPage.value,
      pageSize: pageSize.value,
    });

    if (response.code === 200) {
      meetings.value = response.data.list;
      total.value = response.data.total;
    } else {
      ElMessage.error(response.message || "获取会议列表失败");
    }
  } catch (error) {
    ElMessage.error("获取会议列表失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const getStatusType = (status) => {
  const types = {
    processing: "warning",
    completed: "success",
    failed: "danger",
  };
  return types[status] || "info";
};

const getStatusText = (status) => {
  const texts = {
    processing: "转写中",
    completed: "已完成",
    failed: "失败",
  };
  return texts[status] || "未知";
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchMeetings();
};

const handleFilter = () => {
  currentPage.value = 1;
  fetchMeetings();
};

const handleMeetingClick = (meeting) => {
  router.push(`/meetings/${meeting.meetingId}`);
};

const handleDownload = (meeting) => {
  ElMessage.info("下载功能开发中");
};

const handleShare = (meeting) => {
  ElMessage.info("分享功能开发中");
};

const handleDelete = async (meeting) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除会议"${meeting.title}"吗？删除后将无法恢复。`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await meetingApi.delete(meeting.meetingId);

    if (response.code === 200) {
      ElMessage.success("删除成功");
      fetchMeetings();
    } else {
      ElMessage.error(response.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败：" + error.message);
    }
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchMeetings();
};

onMounted(() => {
  fetchMeetings();
});
</script>

<style lang="scss" scoped>
.meetings-page {
  max-width: 100%;
  padding: 0 16px;
  box-sizing: border-box;

  .search-bar {
    margin-bottom: 16px;
    width: 100%;
    box-sizing: border-box;

    @media screen and (max-width: 768px) {
      :deep(.el-input-group) {
        flex-direction: column;

        .el-input__wrapper {
          border-radius: 4px;
          margin-bottom: 8px;
        }

        .el-input-group__append {
          border-radius: 4px;
        }
      }
    }
  }

  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
    width: 100%;
    box-sizing: border-box;

    @media screen and (max-width: 768px) {
      flex-direction: column;

      .el-select,
      .el-date-editor {
        width: 100% !important;
      }
    }
  }

  .meeting-list {
    display: grid;
    gap: 16px;
    width: 100%;
    box-sizing: border-box;

    @media screen and (min-width: 769px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media screen and (min-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
    }
  }

  .meeting-card {
    background: var(--white);
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid var(--border-color);
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    @media screen and (max-width: 768px) {
      padding: 12px;
    }

    .meeting-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      width: 100%;
      box-sizing: border-box;

      .meeting-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-color);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-right: 8px;
      }
    }

    .meeting-info {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;
      color: var(--text-color-secondary);
      font-size: 14px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      @media screen and (max-width: 768px) {
        flex-wrap: wrap;
        gap: 8px;

        .info-item {
          font-size: 12px;
        }
      }
    }

    .meeting-content {
      color: var(--text-color-secondary);
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meeting-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      box-sizing: border-box;

      .meeting-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        flex: 1;
        overflow: hidden;
      }

      .meeting-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }

      @media screen and (max-width: 768px) {
        flex-wrap: wrap;
        gap: 8px;

        .meeting-tags {
          width: 100%;
        }

        .meeting-actions {
          width: 100%;
          justify-content: flex-start;
        }
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}
</style>
