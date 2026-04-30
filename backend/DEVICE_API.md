# 硬件设备对接API文档

## 一、概述

本文档描述了硬件录音设备如何对接会议录音转写系统。硬件设备完成录音后，通过HTTP接口将录音文件上传到服务器，系统自动完成语音转写并推送到企业微信。

## 二、系统架构

```
┌─────────────────┐      HTTP请求       ┌─────────────────┐
│   硬件录音盒子    │ ────────────────→  │   后端服务器      │
│   (USB麦克风)     │                    │   (Spring Boot)  │
└─────────────────┘                    └────────┬────────┘
                                                │
                    ┌───────────────────────────┼───────────────────────────┐
                    │                           │                           │
                    ▼                           ▼                           ▼
             ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
             │   文件存储    │          │  语音转写     │          │  企业微信    │
             │   (本地)      │          │  (SiliconFlow)│          │   消息推送   │
             └──────────────┘          └──────────────┘          └──────────────┘
```

## 三、接口列表

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 注册设备 | POST | /api/devices/register | 注册硬件设备 |
| 心跳 | POST | /api/devices/heartbeat | 设备保活，报告在线状态 |
| 创建会议 | POST | /api/meetings/create | 创建会议记录 |
| 上传录音 | POST | /api/meetings/upload-audio | 上传录音文件 |
| 更新状态 | POST | /api/meetings/update-status | 更新会议状态 |
| 解除绑定 | POST | /api/devices/unbind | 解除设备与用户的绑定 |

---

## 四、接口详情

### 4.1 注册设备

**功能**：将硬件设备注册到系统，获取设备ID。

**请求**
```http
POST /api/devices/register
Content-Type: application/json

{
  "deviceSerial": "BOX_001",
  "deviceName": "会议室1号录音盒子"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| deviceSerial | String | 是 | 设备序列号（唯一标识），建议使用设备MAC地址或SN号 |
| deviceName | String | 是 | 设备名称，用于后台显示 |

**响应示例**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "deviceId": "DEV_1234567890",
    "deviceSerial": "BOX_001",
    "deviceName": "会议室1号录音盒子",
    "status": "offline"
  }
}
```

**设备序列号命名建议**
- 格式：`BOX_XXX` 或使用设备MAC地址
- 示例：`BOX_001`、`BOX_002`、`AA:BB:CC:DD:EE:FF`

---

### 4.2 设备心跳

**功能**：定期发送心跳，报告设备在线状态。

**请求**
```http
POST /api/devices/heartbeat
Content-Type: application/json

{
  "deviceSerial": "BOX_001"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| deviceSerial | String | 是 | 设备序列号 |

**建议心跳间隔**：30秒

**响应示例**
```json
{
  "code": 200,
  "message": "success"
}
```

---

### 4.3 创建会议

**功能**：在录音开始前创建会议记录。

**请求**
```http
POST /api/meetings/create
Content-Type: application/json

{
  "title": "2024-01-15 10:00 会议室周会",
  "date": "2024-01-15 10:00:00",
  "deviceSerial": "BOX_001",
  "duration": "60"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| title | String | 是 | 会议标题 |
| date | String | 是 | 会议日期时间，格式：`YYYY-MM-DD HH:mm:ss` |
| deviceSerial | String | 是 | 设备序列号 |
| duration | String | 否 | 预计会议时长（分钟） |

**响应示例**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "meetingId": "MTG_1705296000000"
  }
}
```

**重要**：返回的 `meetingId` 需要保存，后续上传录音时需要使用。

---

### 4.4 上传录音文件

**功能**：录音完成后上传录音文件。

**请求**
```http
POST /api/meetings/upload-audio
Content-Type: multipart/form-data

meetingId: MTG_1705296000000
file: [录音文件二进制数据]
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| meetingId | String | 是 | 会议ID（从创建会议接口获取）|
| file | File | 是 | 录音文件 |

**支持格式**
- MP3 (.mp3)
- WAV (.wav)
- M4A (.m4a)
- AMR (.amr)

**文件大小限制**：500MB

**响应示例**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "meetingId": "MTG_1705296000000",
    "audioUrl": "/uploads/1705296000000_meeting.wav",
    "fileName": "1705296000000_meeting.wav"
  }
}
```

---

### 4.5 更新会议状态

**功能**：录音上传完成后，更新会议状态触发后续处理。

**请求**
```http
POST /api/meetings/update-status
Content-Type: application/json

{
  "meetingId": "MTG_1705296000000",
  "status": "completed"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| meetingId | String | 是 | 会议ID |
| status | String | 是 | 状态值：`completed` |

**响应示例**
```json
{
  "code": 200,
  "message": "success"
}
```

**流程说明**：
更新状态为 `completed` 后，系统会自动：
1. 触发语音转写
2. 生成会议摘要
3. 推送给绑定的用户

---

### 4.6 解除设备绑定

**功能**：解除设备与用户的绑定关系。

**请求**
```http
POST /api/devices/unbind
Content-Type: application/json

{
  "deviceSerial": "BOX_001"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "success"
}
```

---

## 五、设备绑定用户

设备需要与用户绑定后，用户才能收到推送通知。

### 5.1 绑定设备

**请求**
```http
POST /api/devices/bind
Content-Type: application/json

{
  "deviceSerial": "BOX_001",
  "userIds": "user001,user002",
  "userNames": "张三,李四"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| deviceSerial | String | 是 | 设备序列号 |
| userIds | String | 是 | 用户ID（企业微信userId），多个用逗号分隔 |
| userNames | String | 是 | 用户姓名，多个用逗号分隔 |

**响应示例**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bindingId": "BIND_1234567890",
    "deviceSerial": "BOX_001",
    "userIds": "user001,user002",
    "userNames": "张三,李四"
  }
}
```

---

## 六、对接流程

### 6.1 完整业务流程

```
硬件设备                              服务器                              企业微信
    │                                    │                                    │
    │ 1.注册设备                          │                                    │
    │────────────────────────────────────→│                                    │
    │                                    │                                    │
    │ 2.开始录音（本地操作）                │                                    │
    │                                    │                                    │
    │ 3.创建会议                          │                                    │
    │────────────────────────────────────→│                                    │
    │            ←─── meetingId ────────│                                    │
    │                                    │                                    │
    │ 4.定时心跳（每30秒）                 │                                    │
    │────────────────────────────────────→│                                    │
    │                                    │                                    │
    │ 5.录音完成                          │                                    │
    │                                    │                                    │
    │ 6.上传录音文件                      │                                    │
    │────────────────────────────────────→│                                    │
    │                                    │ 7.保存文件                          │
    │                                    │──────────→ [文件存储]                │
    │                                    │                                    │
    │ 8.更新状态为completed              │                                    │
    │────────────────────────────────────→│                                    │
    │                                    │ 9.语音转写                         │
    │                                    │──────────→ [SiliconFlow API]        │
    │                                    │                                    │
    │                                    │ 10.推送消息                        │
    │                                    │────────────────────────────────────→│
    │                                    │                                    │ ← 用户收到通知
```

### 6.2 设备端伪代码

```python
# Python示例（伪代码）
import requests
import time

SERVER_URL = "http://你的服务器地址:8080"
DEVICE_SERIAL = "BOX_001"
DEVICE_NAME = "会议室1号盒子"

# 1. 注册设备
def register_device():
    response = requests.post(f"{SERVER_URL}/api/devices/register", json={
        "deviceSerial": DEVICE_SERIAL,
        "deviceName": DEVICE_NAME
    })
    return response.json()

# 2. 创建会议
def create_meeting():
    response = requests.post(f"{SERVER_URL}/api/meetings/create", json={
        "title": "会议录音",
        "date": get_current_time(),
        "deviceSerial": DEVICE_SERIAL,
        "duration": "60"
    })
    return response.json()["data"]["meetingId"]

# 3. 录音上传
def upload_audio(meeting_id, file_path):
    with open(file_path, "rb") as f:
        files = {"file": f}
        data = {"meetingId": meeting_id}
        response = requests.post(f"{SERVER_URL}/api/meetings/upload-audio",
                                data=data, files=files)
    return response.json()

# 4. 更新状态
def update_status(meeting_id):
    response = requests.post(f"{SERVER_URL}/api/meetings/update-status", json={
        "meetingId": meeting_id,
        "status": "completed"
    })
    return response.json()

# 主流程
def main():
    # 注册
    register_device()

    # 开始录音（用户操作）
    input("按回车开始录音...")
    meeting_id = create_meeting()
    print(f"会议ID: {meeting_id}")

    # 心跳线程
    def heartbeat_loop():
        while True:
            requests.post(f"{SERVER_URL}/api/devices/heartbeat",
                        json={"deviceSerial": DEVICE_SERIAL})
            time.sleep(30)

    # 结束录音
    input("按回车结束录音...")

    # 上传
    upload_audio(meeting_id, "recording.wav")

    # 更新状态，触发推送
    update_status(meeting_id)
    print("上传完成！")

if __name__ == "__main__":
    main()
```

### 6.3 C语言示例（嵌入式设备）

```c
#include <stdio.h>
#include <string.h>

// 使用curl库进行HTTP请求
// 请在项目中添加libcurl支持

#define SERVER_URL "http://你的服务器地址:8080"

// 注册设备
int register_device(const char* serial, const char* name) {
    char json[512];
    snprintf(json, sizeof(json),
        "{\"deviceSerial\":\"%s\",\"deviceName\":\"%s\"}", serial, name);

    char cmd[1024];
    snprintf(cmd, sizeof(cmd),
        "curl -X POST %s/api/devices/register "
        "-H 'Content-Type: application/json' "
        "-d '%s'", SERVER_URL, json);

    return system(cmd);
}

// 上传录音文件
int upload_audio(const char* meeting_id, const char* file_path) {
    char cmd[2048];
    snprintf(cmd, sizeof(cmd),
        "curl -X POST %s/api/meetings/upload-audio "
        "-F 'meetingId=%s' "
        "-F 'file=@%s'", SERVER_URL, meeting_id, file_path);

    return system(cmd);
}
```

---

## 七、错误码

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查参数格式 |
| 404 | 设备/会议不存在 | 检查序列号或会议ID |
| 500 | 服务器内部错误 | 重试或联系管理员 |

**常见错误信息**
- `"设备不存在"` - 请先调用注册接口
- `"会议不存在"` - 检查meetingId是否正确
- `"没有绑定用户"` - 请先调用绑定接口
- `"文件过大"` - 文件超过500MB限制

---

## 八、注意事项

### 8.1 网络要求
- 设备需要能访问服务器地址
- 建议使用有线网络以保证上传稳定性
- 大文件建议分片上传（未来版本支持）

### 8.2 安全性
- 生产环境建议添加设备认证机制
- 可以扩展使用设备密钥进行签名验证

### 8.3 可靠性
- 建议添加重试机制
- 上传失败时保存本地文件，稍后重试
- 日志记录便于排查问题

### 8.4 性能
- 录音文件建议压缩后再上传
- 建议使用流式上传，避免内存不足

---

## 九、联系方式

如有问题，请联系开发团队。
