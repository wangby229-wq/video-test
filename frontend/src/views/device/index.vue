<template>
  <div class="device-page">
    <div class="page-header">
      <h2>设备管理</h2>
      <el-button type="primary" @click="showRegisterDialog()">
        <el-icon><Plus /></el-icon>
        注册设备
      </el-button>
    </div>

    <div class="device-list">
      <el-table :data="devices" v-loading="loading" stripe>
        <el-table-column
          prop="deviceSerial"
          label="设备序列号"
          min-width="120"
        />
        <el-table-column prop="deviceName" label="设备名称" min-width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'online' ? 'success' : 'info'"
              size="small"
            >
              {{ row.status === "online" ? "在线" : "离线" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定人员" min-width="150">
          <template #default="{ row }">
            <span v-if="row.binding">已绑定</span>
            <span v-else class="text-gray">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.binding">
              <el-button
                text
                type="primary"
                size="small"
                @click="showUnbindDialog(row)"
              >
                解除绑定
              </el-button>
            </template>
            <template v-else>
              <el-button
                text
                type="primary"
                size="small"
                @click="showBindDialog(row)"
              >
                绑定
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="registerDialogVisible"
      title="注册设备"
      width="90%"
      max-width="500px"
    >
      <el-form :model="registerForm" label-width="100px">
        <el-form-item label="设备序列号">
          <el-input
            v-model="registerForm.deviceSerial"
            placeholder="请输入设备序列号"
          />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input
            v-model="registerForm.deviceName"
            placeholder="请输入设备名称"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleRegisterSubmit"
          :loading="submitting"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="bindDialogVisible"
      title="设备绑定"
      width="90%"
      max-width="500px"
    >
      <el-form :model="bindForm" label-width="100px">
        <el-form-item label="设备序列号">
          <el-input
            v-model="bindForm.deviceSerial"
            placeholder="请输入设备序列号"
          />
        </el-form-item>
        <el-form-item label="绑定人员">
          <el-select
            v-model="bindForm.selectedUsers"
            multiple
            filterable
            placeholder="请选择绑定人员"
            style="width: 100%"
          >
            <el-option
              v-for="user in corpUsers"
              :key="user.userId"
              :label="user.name"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleBindSubmit"
          :loading="submitting"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <el-dialog v-model="unbindDialogVisible" title="解除绑定" width="400px">
      <p>
        确定要解除设备 <strong>{{ unbindForm.deviceSerial }}</strong> 的绑定吗？
      </p>
      <template #footer>
        <el-button @click="unbindDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          @click="handleUnbindSubmit"
          :loading="submitting"
          >确定解除</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { deviceApi } from "@/api/device";

const loading = ref(false);
const submitting = ref(false);
const devices = ref([]);
const bindings = ref([]);
const corpUsers = ref([]);
const registerDialogVisible = ref(false);
const bindDialogVisible = ref(false);
const unbindDialogVisible = ref(false);

const registerForm = ref({
  deviceSerial: "",
  deviceName: "",
});

const bindForm = ref({
  deviceSerial: "",
  selectedUsers: [],
});

const unbindForm = ref({
  deviceSerial: "",
});

const formatTime = (timeStr) => {
  if (!timeStr) return "-";
  const date = new Date(timeStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const loadDevices = async () => {
  loading.value = true;
  try {
    const response = await deviceApi.getList();
    if (response.code === 200) {
      const deviceList = response.data || [];
      devices.value = deviceList.map((device) => {
        const binding = bindings.value.find(
          (b) => b.deviceSerial === device.deviceSerial,
        );
        return { ...device, binding };
      });
    }
  } catch (error) {
    ElMessage.error("加载设备列表失败");
  } finally {
    loading.value = false;
  }
};

const loadBindings = async () => {
  try {
    const response = await deviceApi.getBindings();
    if (response.code === 200) {
      bindings.value = response.data || [];
    }
  } catch (error) {
    console.error("加载绑定信息失败:", error);
  }
};

const loadCorpUsers = async () => {
  try {
    const response = await deviceApi.getCorpUsers();
    if (response.code === 200) {
      corpUsers.value = response.data || [];
    } else {
      corpUsers.value = [
        { userId: "QianChen", name: "钱辰" },
        { userId: "FangYiMing", name: "方一鸣" },
        { userId: "SuZhouSanNiu", name: "王犇逸" },
        { userId: "WuGang", name: "吴刚" },
      ];
    }
  } catch (error) {
    console.error("加载企业成员失败:", error);
    corpUsers.value = [
      { userId: "QianChen", name: "钱辰" },
      { userId: "FangYiMing", name: "方一鸣" },
      { userId: "SuZhouSanNiu", name: "王犇逸" },
      { userId: "WuGang", name: "吴刚" },
    ];
  }
};

const showRegisterDialog = () => {
  registerForm.value.deviceSerial = "";
  registerForm.value.deviceName = "";
  registerDialogVisible.value = true;
};

const handleRegisterSubmit = async () => {
  if (!registerForm.value.deviceSerial) {
    ElMessage.warning("请输入设备序列号");
    return;
  }
  if (!registerForm.value.deviceName) {
    ElMessage.warning("请输入设备名称");
    return;
  }

  submitting.value = true;
  try {
    const response = await deviceApi.register({
      deviceSerial: registerForm.value.deviceSerial,
      deviceName: registerForm.value.deviceName,
    });

    if (response.code === 200) {
      ElMessage.success("设备注册成功");
      registerDialogVisible.value = false;
      loadDevices();
    } else {
      ElMessage.error(response.message || "注册失败");
    }
  } catch (error) {
    ElMessage.error("注册失败：" + (error.message || "网络错误"));
  } finally {
    submitting.value = false;
  }
};

const showBindDialog = (row) => {
  bindForm.value.deviceSerial = row ? row.deviceSerial : "";
  bindForm.value.selectedUsers = [];
  bindDialogVisible.value = true;
};

const showUnbindDialog = (row) => {
  unbindForm.value.deviceSerial = row.deviceSerial;
  unbindDialogVisible.value = true;
};

const handleBindSubmit = async () => {
  if (!bindForm.value.deviceSerial) {
    ElMessage.warning("请输入设备序列号");
    return;
  }
  if (bindForm.value.selectedUsers.length === 0) {
    ElMessage.warning("请选择至少一个绑定人员");
    return;
  }

  submitting.value = true;
  try {
    const selectedUserIds = bindForm.value.selectedUsers.join(",");
    const selectedUserNames = bindForm.value.selectedUsers
      .map((id) => corpUsers.value.find((u) => u.userId === id)?.name || id)
      .join(",");

    const response = await deviceApi.bind({
      deviceSerial: bindForm.value.deviceSerial,
      userIds: selectedUserIds,
      userNames: selectedUserNames,
    });

    if (response.code === 200) {
      ElMessage.success("绑定成功");
      bindDialogVisible.value = false;
      await loadBindings();
      await loadDevices();
    } else {
      ElMessage.error(response.message || "绑定失败");
    }
  } catch (error) {
    ElMessage.error("绑定失败：" + (error.message || "网络错误"));
  } finally {
    submitting.value = false;
  }
};

const handleUnbindSubmit = async () => {
  submitting.value = true;
  try {
    const response = await deviceApi.unbind(unbindForm.value.deviceSerial);
    if (response.code === 200) {
      ElMessage.success("解绑成功");
      unbindDialogVisible.value = false;
      await loadBindings();
      await loadDevices();
    } else {
      ElMessage.error(response.message || "解绑失败");
    }
  } catch (error) {
    ElMessage.error("解绑失败：" + (error.message || "网络错误"));
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  await loadBindings();
  await loadDevices();
  loadCorpUsers();
});
</script>

<style lang="scss" scoped>
.device-page {
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

  .text-gray {
    color: #999;
  }

  :deep(.el-table) {
    font-size: 13px;

    .el-table__header th {
      font-weight: 600;
    }
  }

  :deep(.el-dialog) {
    .el-form-item {
      margin-bottom: 18px;
    }
  }

  @media screen and (max-width: 768px) {
    padding: 12px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 12px;

      h2 {
        font-size: 16px;
      }
    }

    :deep(.el-table) {
      font-size: 12px;

      .el-table__header th {
        padding: 8px 4px;
        font-size: 12px;
      }

      .el-table__body td {
        padding: 8px 4px;
      }
    }

    :deep(.el-button) {
      font-size: 12px;
      padding: 4px 8px;
    }
  }
}
</style>
