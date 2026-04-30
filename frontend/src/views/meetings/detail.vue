<template>
  <div class="meeting-detail" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 会议信息 -->
    <div class="meeting-info-card">
      <div class="meeting-header">
        <h2 class="meeting-title">{{ meeting.title }}</h2>
        <el-tag :type="getStatusType(meeting.status)" size="large">
          {{ getStatusText(meeting.status) }}
        </el-tag>
      </div>
      <div class="meeting-meta">
        <div class="meta-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ meeting.date }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Clock /></el-icon>
          <span>{{ meeting.duration || "未知" }}</span>
        </div>
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span>{{ meeting.participants }}人参与</span>
        </div>
      </div>
    </div>

    <!-- 音频播放器 -->
    <div class="audio-player-card" v-if="meeting.audioUrl">
      <div class="player-header">
        <el-icon><Headset /></el-icon>
        <span>录音播放</span>
      </div>
      <div class="player-content">
        <audio
          ref="audioRef"
          :src="getFullAudioUrl(meeting.audioUrl)"
          controls
          style="width: 100%"
        ></audio>
      </div>
      <div class="player-actions">
        <el-button type="primary" @click="handleDownloadAudio">
          <el-icon><Download /></el-icon>
          下载录音
        </el-button>
        <el-button @click="handleShareAudio">
          <el-icon><Share /></el-icon>
          分享录音
        </el-button>
      </div>
    </div>

    <!-- 转写内容 -->
    <div class="transcript-card" v-if="transcript.length > 0">
      <div class="transcript-header">
        <div class="header-left">
          <el-icon><Document /></el-icon>
          <span>转写内容</span>
        </div>
        <div class="header-right">
          <el-button text @click="handleCopyTranscript">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
          <el-button text @click="handleDownloadTranscript">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>
      <div class="transcript-content">
        <div
          v-for="(item, index) in transcript"
          :key="index"
          class="transcript-item"
        >
          <div class="speaker">{{ item.speaker }}</div>
          <div class="time">{{ item.time }}</div>
          <div class="text">{{ item.text }}</div>
        </div>
      </div>
    </div>

    <!-- 会议摘要 -->
    <div class="summary-card">
      <div class="summary-header">
        <div class="header-left">
          <el-icon><Reading /></el-icon>
          <span>会议摘要</span>
          <el-tag
            v-if="summaryStatus === 'generating'"
            type="warning"
            size="small"
          >
            生成中...
          </el-tag>
          <el-tag
            v-else-if="summaryStatus === 'completed'"
            type="success"
            size="small"
          >
            已完成
          </el-tag>
          <el-tag
            v-else-if="summaryStatus === 'failed'"
            type="danger"
            size="small"
          >
            生成失败
          </el-tag>
        </div>
        <div class="header-right">
          <el-button
            v-if="meeting.audioUrl && summaryStatus !== 'generating'"
            type="primary"
            @click="handleGenerateSummary"
          >
            <el-icon><MagicStick /></el-icon>
            生成摘要
          </el-button>
          <el-button v-if="summaryStatus === 'generating'" type="info" disabled>
            <el-icon class="is-loading"><Loading /></el-icon>
            生成中...
          </el-button>
        </div>
      </div>
      <div class="summary-content">
        <div v-if="summaryStatus === 'generating'" class="generating-animation">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在生成会议摘要，请稍候...</p>
          <p class="hint">您可以继续浏览其他内容</p>
        </div>
        <div v-else-if="meeting.summary" v-html="meeting.summary"></div>
        <div v-else class="no-summary">
          <p>暂无摘要</p>
          <p class="hint">点击上方"生成摘要"按钮开始生成</p>
        </div>
      </div>
      <div class="summary-tags">
        <el-tag v-for="tag in meeting.tags" :key="tag" effect="plain">
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { meetingApi } from "@/api/meeting";

const route = useRoute();
const router = useRouter();
const audioRef = ref(null);
const loading = ref(true);

const meeting = ref({
  meetingId: "",
  title: "",
  date: "",
  duration: "",
  participants: 0,
  status: "",
  audioUrl: "",
  summary: "",
  tags: [],
});

const transcript = ref([]);
const summaryStatus = ref("idle");
let statusPollingInterval = null;

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

const getFullAudioUrl = (audioUrl) => {
  if (!audioUrl) return "";
  // 如果是相对路径，添加完整的后端地址
  if (audioUrl.startsWith("/")) {
    return window.API_CONFIG.API_BASE_URL + audioUrl;
  }
  return audioUrl;
};

const handleBack = () => {
  router.push("/meetings");
};

const handleDownloadAudio = () => {
  if (meeting.value.audioUrl) {
    const link = document.createElement("a");
    link.href = getFullAudioUrl(meeting.value.audioUrl);
    link.download = meeting.value.title + ".mp3";
    link.click();
  } else {
    ElMessage.warning("暂无录音文件");
  }
};

const handleShareAudio = () => {
  ElMessage.success("分享链接已复制到剪贴板");
};

const handleCopyTranscript = () => {
  const text = transcript.value
    .map((item) => `${item.speaker} ${item.time}\n${item.text}`)
    .join("\n\n");
  navigator.clipboard.writeText(text);
  ElMessage.success("转写内容已复制到剪贴板");
};

const handleDownloadTranscript = () => {
  ElMessage.success("开始下载转写文档");
};

const fetchMeetingDetail = async () => {
  const meetingId = route.params.id;
  loading.value = true;
  try {
    const response = await meetingApi.getDetail(meetingId);
    if (response.code === 200) {
      meeting.value = response.data.meeting;
      transcript.value = response.data.transcripts || [];
      summaryStatus.value = response.data.meeting.summaryStatus || "idle";

      if (summaryStatus.value === "generating") {
        startPollingSummaryStatus();
      }
    } else {
      ElMessage.error(response.message || "获取会议详情失败");
    }
  } catch (error) {
    ElMessage.error("获取会议详情失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const handleGenerateSummary = async () => {
  try {
    const response = await meetingApi.generateSummary(meeting.value.meetingId);
    if (response.code === 200) {
      summaryStatus.value = "generating";
      ElMessage.success("摘要生成已开始，请稍候...");
      startPollingSummaryStatus();
    } else {
      ElMessage.error(response.message || "生成摘要失败");
    }
  } catch (error) {
    ElMessage.error("生成摘要失败：" + error.message);
  }
};

const startPollingSummaryStatus = () => {
  if (statusPollingInterval) {
    clearInterval(statusPollingInterval);
  }

  statusPollingInterval = setInterval(async () => {
    try {
      const response = await meetingApi.getSummaryStatus(
        meeting.value.meetingId,
      );
      if (response.code === 200) {
        const status = response.data.status;
        summaryStatus.value = status;

        if (status === "completed") {
          meeting.value.summary = response.data.summary;
          clearInterval(statusPollingInterval);
          statusPollingInterval = null;
          ElMessage.success("摘要生成完成！");
        } else if (status === "failed") {
          clearInterval(statusPollingInterval);
          statusPollingInterval = null;
          ElMessage.error("摘要生成失败，请重试");
        }
      }
    } catch (error) {
      console.error("轮询摘要状态失败:", error);
    }
  }, 3000);
};

const stopPollingSummaryStatus = () => {
  if (statusPollingInterval) {
    clearInterval(statusPollingInterval);
    statusPollingInterval = null;
  }
};

onMounted(() => {
  fetchMeetingDetail();
});

onUnmounted(() => {
  stopPollingSummaryStatus();
});
</script>

<style lang="scss" scoped>
.meeting-detail {
  .back-bar {
    margin-bottom: 16px;
  }

  .meeting-info-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;

    .meeting-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      width: 100%;
      box-sizing: border-box;

      .meeting-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color);
        margin: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
      }
    }

    .meeting-meta {
      display: flex;
      gap: 24px;
      color: var(--text-color-secondary);
      font-size: 14px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    @media screen and (max-width: 768px) {
      padding: 16px;

      .meeting-header {
        flex-wrap: wrap;
        gap: 8px;

        .meeting-title {
          font-size: 18px;
          width: 100%;
        }
      }

      .meeting-meta {
        flex-wrap: wrap;
        gap: 12px;

        .meta-item {
          font-size: 12px;
        }
      }
    }
  }

  .audio-player-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;

    @media screen and (max-width: 768px) {
      padding: 16px;
    }

    .player-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .player-content {
      margin-bottom: 16px;
    }

    .player-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-start;
      width: 100%;
      box-sizing: border-box;

      @media screen and (max-width: 768px) {
        flex-direction: row;
        justify-content: space-between;
        gap: 8px;

        .el-button {
          flex: 1;
          min-width: 0;
        }
      }
    }
  }

  .transcript-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;

    .transcript-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
      }

      .header-right {
        display: flex;
        gap: 8px;
      }
    }

    .transcript-content {
      max-height: 400px;
      overflow-y: auto;

      .transcript-item {
        padding: 12px;
        border-bottom: 1px solid var(--border-color);

        &:last-child {
          border-bottom: none;
        }

        .speaker {
          font-weight: 600;
          color: var(--primary-color);
          margin-bottom: 4px;
        }

        .time {
          font-size: 12px;
          color: var(--text-color-secondary);
          margin-bottom: 8px;
        }

        .text {
          line-height: 1.6;
          color: var(--text-color);
        }
      }
    }
  }

  .summary-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;

    @media screen and (max-width: 768px) {
      padding: 16px;

      .summary-header {
        flex-wrap: wrap;
        gap: 8px;

        .header-left,
        .header-right {
          width: 100%;
        }

        .header-right {
          justify-content: flex-start;
        }
      }
    }

    .summary-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        overflow: hidden;
        flex: 1;
        min-width: 0;
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;
      }
    }

    .summary-content {
      line-height: 1.8;
      color: var(--text-color);
      margin-bottom: 16px;
      padding: 20px;
      background: #f9f9f9;
      border-radius: 8px;
      max-height: 300px;
      overflow-y: auto;

      .generating-animation {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px 20px;
        color: var(--text-color-secondary);
        text-align: center;

        .is-loading {
          font-size: 32px;
          color: var(--el-color-primary);
          margin-bottom: 16px;
          animation: rotate 1s linear infinite;
        }

        p {
          margin: 8px 0;
        }

        .hint {
          font-size: 14px;
          color: var(--text-color-secondary);
        }
      }

      .no-summary {
        text-align: center;
        padding: 40px 20px;
        color: var(--text-color-secondary);

        p {
          margin: 8px 0;
        }

        .hint {
          font-size: 14px;
          color: var(--text-color-secondary);
        }
      }
    }

    .summary-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  @media screen and (max-width: 768px) {
    .meeting-info-card {
      padding: 16px;

      .meeting-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        .meeting-title {
          font-size: 20px;
        }
      }

      .meeting-meta {
        flex-direction: column;
        gap: 8px;
      }
    }

    .audio-player-card,
    .transcript-card,
    .summary-card {
      padding: 16px;
    }

    .player-actions {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
