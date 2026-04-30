<template>
  <div class="upload-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button type="text" @click="handleBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>上传录音文件</h2>
    </div>

    <!-- 上传区域 -->
    <div class="upload-card">
      <div class="upload-header">
        <el-icon><Upload /></el-icon>
        <span>上传文件</span>
      </div>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :file-list="fileList"
        :limit="1"
        accept="audio/*,.mp3,.wav,.m4a,.amr"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将录音文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 mp3、wav、m4a、amr 格式，文件大小不超过 500MB
          </div>
        </template>
      </el-upload>
    </div>

    <!-- 会议信息 -->
    <div class="info-card" v-if="fileList.length > 0">
      <div class="info-header">
        <el-icon><Edit /></el-icon>
        <span>会议信息</span>
      </div>

      <el-form :model="meetingForm" label-width="100px" class="meeting-form">
        <el-form-item label="会议标题">
          <el-input v-model="meetingForm.title" placeholder="请输入会议标题" />
        </el-form-item>
        <el-form-item label="会议日期">
          <el-date-picker
            v-model="meetingForm.date"
            type="datetime"
            placeholder="选择会议日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="参与人员">
          <el-input
            v-model="meetingForm.participants"
            placeholder="请输入参与人员，用逗号分隔"
          />
        </el-form-item>
        <el-form-item label="会议标签">
          <el-select
            v-model="meetingForm.tags"
            multiple
            filterable
            allow-create
            placeholder="选择或创建标签"
            style="width: 100%"
          >
            <el-option label="产品" value="产品" />
            <el-option label="技术" value="技术" />
            <el-option label="设计" value="设计" />
            <el-option label="运营" value="运营" />
            <el-option label="例会" value="例会" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议备注">
          <el-input
            v-model="meetingForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入会议备注信息"
          />
        </el-form-item>
      </el-form>
    </div>

    <!-- 转写设置 -->
    <div class="settings-card" v-if="fileList.length > 0">
      <div class="settings-header">
        <el-icon><Setting /></el-icon>
        <span>转写设置</span>
      </div>

      <el-form
        :model="transcribeSettings"
        label-width="120px"
        class="settings-form"
      >
        <el-form-item label="识别语言">
          <el-select v-model="transcribeSettings.language" style="width: 100%">
            <el-option label="中文普通话" value="zh-CN" />
            <el-option label="中文粤语" value="zh-HK" />
            <el-option label="英语" value="en-US" />
            <el-option label="日语" value="ja-JP" />
          </el-select>
        </el-form-item>
        <el-form-item label="说话人识别">
          <el-switch v-model="transcribeSettings.speakerDiarization" />
          <span class="form-tip">自动识别不同说话人</span>
        </el-form-item>
        <el-form-item label="自动摘要">
          <el-switch v-model="transcribeSettings.autoSummary" />
          <span class="form-tip">自动生成会议摘要</span>
        </el-form-item>
        <el-form-item label="关键词提取">
          <el-switch v-model="transcribeSettings.keywordExtraction" />
          <span class="form-tip">自动提取会议关键词</span>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar" v-if="fileList.length > 0">
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="uploading">
        {{ uploading ? "上传中..." : "开始上传并转写" }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { meetingApi } from "@/api/meeting";

const router = useRouter();
const uploadRef = ref(null);
const fileList = ref([]);
const uploading = ref(false);

const meetingForm = ref({
  title: "",
  date: "",
  participants: "",
  tags: [],
  remark: "",
});

const transcribeSettings = ref({
  language: "zh-CN",
  speakerDiarization: true,
  autoSummary: true,
  keywordExtraction: true,
});

const handleFileChange = (file, files) => {
  fileList.value = files;

  // 自动填充文件名作为标题
  if (!meetingForm.value.title && file.name) {
    meetingForm.value.title = file.name.replace(/\.[^/.]+$/, "");
  }
};

const handleFileRemove = (file, files) => {
  fileList.value = files;
};

const handleReset = () => {
  fileList.value = [];
  meetingForm.value = {
    title: "",
    date: "",
    participants: "",
    tags: [],
    remark: "",
  };
  uploadRef.value?.clearFiles();
};

const handleBack = () => {
  router.push("/meetings");
};

const handleSubmit = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning("请先上传录音文件");
    return;
  }

  if (!meetingForm.value.title) {
    ElMessage.warning("请输入会议标题");
    return;
  }

  uploading.value = true;

  try {
    // 1. 创建会议记录
    const createResponse = await meetingApi.create({
      title: meetingForm.value.title,
      date: meetingForm.value.date
        ? new Date(meetingForm.value.date)
            .toISOString()
            .slice(0, 19)
            .replace("T", " ")
        : new Date().toISOString().slice(0, 19).replace("T", " "),
      tags: meetingForm.value.tags,
    });

    if (createResponse.code !== 200) {
      throw new Error("创建会议失败：" + createResponse.message);
    }

    const meetingId = createResponse.data.meetingId;

    // 2. 上传音频文件
    const formData = new FormData();
    formData.append("file", fileList.value[0].raw);
    formData.append("meetingId", meetingId);

    const uploadResponse = await meetingApi.uploadAudio(formData);

    if (uploadResponse.code !== 200) {
      throw new Error("上传文件失败：" + uploadResponse.message);
    }

    // 3. 更新会议状态为已完成
    const statusResponse = await meetingApi.updateStatus(
      meetingId,
      "completed"
    );
    if (statusResponse.code !== 200) {
      ElMessage.warning("上传成功，但更新状态失败：" + statusResponse.message);
    }

    // 4. 推送录音文件给当前用户
    const pushResponse = await meetingApi.pushAudio(meetingId);
    if (pushResponse.code === 200) {
      ElMessage.success("上传成功，录音文件已推送至企业微信");
    } else {
      ElMessage.warning("上传成功，但推送失败：" + pushResponse.message);
    }

    router.push("/meetings");
  } catch (error) {
    ElMessage.error("上传失败：" + error.message);
  } finally {
    uploading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.upload-page {
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);

    .back-button {
      margin-right: 16px;
      font-size: 16px;
    }

    h2 {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      color: var(--text-color);
    }
  }

  .upload-card,
  .info-card,
  .settings-card {
    background: var(--white);
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;
  }

  .upload-header,
  .info-header,
  .settings-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  .upload-area {
    :deep(.el-upload-dragger) {
      width: 100%;
    }
  }

  .meeting-form,
  .settings-form {
    .form-tip {
      margin-left: 12px;
      color: var(--text-color-secondary);
      font-size: 12px;
    }
  }

  .action-bar {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px;
    background: var(--white);
    border-radius: 8px;
  }

  @media screen and (max-width: 768px) {
    .upload-card,
    .info-card,
    .settings-card {
      padding: 16px;
    }

    .action-bar {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }

    .meeting-form,
    .settings-form {
      :deep(.el-form-item__label) {
        text-align: left;
        width: 100% !important;
        margin-bottom: 8px;
      }

      :deep(.el-form-item__content) {
        margin-left: 0 !important;
      }
    }
  }
}
</style>
