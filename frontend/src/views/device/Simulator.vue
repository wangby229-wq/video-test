<template>
  <div class="simulator-page">
    <div class="page-header">
      <h2>音频推送模拟器</h2>
      <el-button @click="loadData" :loading="loading">刷新数据</el-button>
    </div>

    <div class="simulator-content">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12">
          <el-card class="setup-card">
            <template #header>
              <div class="card-header">
                <span>1. 选择设备并发送心跳</span>
              </div>
            </template>

            <el-form label-width="120px">
              <el-form-item label="选择设备">
                <el-select
                  v-model="selectedDevice"
                  placeholder="请选择设备"
                  style="width: 100%"
                >
                  <el-option
                    v-for="device in devices"
                    :key="device.deviceSerial"
                    :label="`${device.deviceName} (${device.deviceSerial})`"
                    :value="device.deviceSerial"
                  >
                    <span>{{ device.deviceName }}</span>
                    <el-tag
                      :type="device.status === 'online' ? 'success' : 'info'"
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ device.status === "online" ? "在线" : "离线" }}
                    </el-tag>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="心跳间隔">
                <el-input-number
                  v-model="heartbeatInterval"
                  :min="5"
                  :max="60"
                />
                <span style="margin-left: 8px">秒</span>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="startHeartbeat"
                  :disabled="!selectedDevice || heartbeatRunning"
                >
                  {{ heartbeatRunning ? "心跳运行中..." : "启动心跳" }}
                </el-button>
                <el-button @click="stopHeartbeat" :disabled="!heartbeatRunning"
                  >停止心跳</el-button
                >
              </el-form-item>
            </el-form>

            <el-alert
              v-if="heartbeatRunning"
              :title="`心跳发送中... 每${heartbeatInterval}秒发送一次`"
              type="success"
              :closable="false"
              show-icon
            />
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-card class="setup-card">
            <template #header>
              <div class="card-header">
                <span>2. 模拟录音并推送</span>
              </div>
            </template>

            <el-form label-width="120px">
              <el-form-item label="会议标题">
                <el-input
                  v-model="meetingForm.title"
                  placeholder="请输入会议标题"
                />
              </el-form-item>

              <el-form-item label="会议时长">
                <el-input-number
                  v-model="meetingForm.duration"
                  :min="1"
                  :max="120"
                />
                <span style="margin-left: 8px">分钟</span>
              </el-form-item>

              <el-form-item label="会议日期">
                <el-date-picker
                  v-model="meetingForm.date"
                  type="datetime"
                  placeholder="选择会议日期时间"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="绑定用户">
                <el-tag
                  v-for="binding in deviceBindings"
                  :key="binding.deviceSerial"
                  type="info"
                  style="margin-right: 8px"
                >
                  {{ binding.userNames || binding.userIds }}
                </el-tag>
                <span v-if="deviceBindings.length === 0" class="text-gray"
                  >该设备未绑定用户</span
                >
              </el-form-item>

              <el-form-item>
                <el-button
                  type="success"
                  @click="simulateRecording"
                  :loading="simulating"
                  :disabled="!selectedDevice"
                >
                  {{ simulating ? "模拟中..." : "模拟录音推送" }}
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="result-card" v-if="pushResults.length > 0">
        <template #header>
          <div class="card-header">
            <span>推送结果</span>
            <el-button size="small" @click="pushResults = []">清空</el-button>
          </div>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="(result, index) in pushResults"
            :key="index"
            :timestamp="result.time"
            :type="result.success ? 'success' : 'danger'"
          >
            <h4>{{ result.title }}</h4>
            <p>会议ID: {{ result.meetingId }}</p>
            <p>设备: {{ result.deviceSerial }}</p>
            <p>推送用户: {{ result.users }}</p>
            <p v-if="result.success" style="color: green">✓ 推送成功</p>
            <p v-else style="color: red">✗ 推送失败: {{ result.error }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>使用说明</span>
          </div>
        </template>

        <el-steps :active="4" align-center>
          <el-step title="步骤1" description="选择设备并发送心跳" />
          <el-step title="步骤2" description="填写会议信息" />
          <el-step title="步骤3" description="点击模拟录音" />
          <el-step title="步骤4" description="自动推送给绑定用户" />
        </el-steps>

        <el-divider />

        <el-alert title="模拟说明" type="info" :closable="false">
          <template #default>
            <ul style="margin: 0; padding-left: 20px">
              <li>此工具用于模拟真实设备发送音频的完整流程</li>
              <li>心跳用于保持设备"在线"状态</li>
              <li>模拟录音会自动创建会议并推送给所有绑定该设备的用户</li>
              <li>推送成功后，企业微信用户会收到会议录音通知</li>
            </ul>
          </template>
        </el-alert>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { deviceApi } from "@/api/device";
import { meetingApi } from "@/api/meeting";
import { simulatorApi } from "@/api/simulator";

const loading = ref(false);
const simulating = ref(false);
const heartbeatRunning = ref(false);
const heartbeatInterval = ref(10);
const heartbeatTimer = ref(null);

const devices = ref([]);
const bindings = ref([]);

const selectedDevice = ref("");
const heartbeatCount = ref(0);

const meetingForm = ref({
  title: "",
  date: new Date(),
  duration: 30,
});

const pushResults = ref([]);

const deviceBindings = computed(() => {
  if (!selectedDevice.value) return [];
  return bindings.value.filter((b) => b.deviceSerial === selectedDevice.value);
});

const loadData = async () => {
  loading.value = true;
  try {
    const [devicesRes, bindingsRes] = await Promise.all([
      deviceApi.getList(),
      deviceApi.getBindings(),
    ]);

    if (devicesRes.code === 200) {
      devices.value = devicesRes.data || [];
    }

    if (bindingsRes.code === 200) {
      bindings.value = bindingsRes.data || [];
    }
  } catch (error) {
    ElMessage.error("加载数据失败");
  } finally {
    loading.value = false;
  }
};

const startHeartbeat = async () => {
  if (!selectedDevice.value) {
    ElMessage.warning("请先选择设备");
    return;
  }

  await sendHeartbeat();

  heartbeatRunning.value = true;
  heartbeatTimer.value = setInterval(async () => {
    await sendHeartbeat();
  }, heartbeatInterval.value * 1000);

  ElMessage.success("心跳已启动");
};

const stopHeartbeat = () => {
  if (heartbeatTimer.value) {
    clearInterval(heartbeatTimer.value);
    heartbeatTimer.value = null;
  }
  heartbeatRunning.value = false;
  heartbeatCount.value = 0;
  ElMessage.info("心跳已停止");
};

const sendHeartbeat = async () => {
  try {
    await simulatorApi.heartbeat(selectedDevice.value);
    heartbeatCount.value++;

    const device = devices.value.find(
      (d) => d.deviceSerial === selectedDevice.value,
    );
    if (device) {
      device.status = "online";
    }
  } catch (error) {
    console.error("心跳发送失败:", error);
  }
};

const simulateRecording = async () => {
  if (!selectedDevice.value) {
    ElMessage.warning("请先选择设备");
    return;
  }

  const device = devices.value.find(
    (d) => d.deviceSerial === selectedDevice.value,
  );
  if (!device) {
    ElMessage.warning("设备不存在");
    return;
  }

  if (deviceBindings.value.length === 0) {
    ElMessage.warning("该设备未绑定任何用户，无法推送");
    return;
  }

  simulating.value = true;

  try {
    const title = meetingForm.value.title || `模拟会议_${Date.now()}`;

    const createRes = await meetingApi.create({
      title: title,
      date: meetingForm.value.date
        ? new Date(meetingForm.value.date)
            .toISOString()
            .slice(0, 19)
            .replace("T", " ")
        : new Date().toISOString().slice(0, 19).replace("T", " "),
      deviceSerial: selectedDevice.value,
      duration: meetingForm.value.duration,
    });

    if (createRes.code !== 200) {
      throw new Error(createRes.message || "创建会议失败");
    }

    const meetingId = createRes.data.meetingId;

    await meetingApi.updateStatus(meetingId, "completed");

    const userNames = deviceBindings.value
      .map((b) => b.userNames || b.userIds)
      .join(", ");

    pushResults.value.unshift({
      time: new Date().toLocaleString(),
      title: title,
      meetingId: meetingId,
      deviceSerial: selectedDevice.value,
      users: userNames,
      success: true,
      error: null,
    });

    ElMessage.success("模拟推送成功！绑定的用户已收到通知");
  } catch (error) {
    ElMessage.error("模拟失败：" + error.message);

    pushResults.value.unshift({
      time: new Date().toLocaleString(),
      title: meetingForm.value.title || "模拟会议",
      meetingId: "-",
      deviceSerial: selectedDevice.value,
      users: deviceBindings.value
        .map((b) => b.userNames || b.userIds)
        .join(", "),
      success: false,
      error: error.message,
    });
  } finally {
    simulating.value = false;
  }
};

onMounted(() => {
  loadData();
});

onUnmounted(() => {
  stopHeartbeat();
});
</script>

<style lang="scss" scoped>
.simulator-page {
  padding: 16px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      font-size: 18px;
    }
  }

  .simulator-content {
    .setup-card {
      margin-bottom: 16px;
    }

    .result-card {
      margin-bottom: 16px;
    }

    .info-card {
      margin-bottom: 16px;
    }

    .text-gray {
      color: #999;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  @media screen and (max-width: 768px) {
    padding: 12px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }
}
</style>
