# Android硬件设备对接接口文档

## 一、接口概述

Android设备需要三个接口：

1. **注册设备** - 盒子首次使用时注册
2. **绑定用户** - 绑定后才知道推送给谁
3. **录音上传** - 录音完成后自动上传+转写+推送

### 完整流程

```
盒子首次开机
    ↓
1. 注册设备
    ↓
2. 绑定用户（可以写死在APP里，或让管理员配置）
    ↓
日常使用：
3. 录音完成 → 上传 → 自动推送给绑定的用户
```

---

## 二、接口详情

### 接口1：注册设备

**请求URL**

```
POST http://服务器地址:8080/api/devices/register
Content-Type: application/json
```

**请求参数**

```json
{
  "deviceSerial": "BOX_001",
  "deviceName": "会议室1号盒子"
}
```

| 参数         | 类型   | 必填 | 说明                                        |
| ------------ | ------ | ---- | ------------------------------------------- |
| deviceSerial | String | 是   | 设备序列号（唯一标识），建议使用设备MAC地址 |
| deviceName   | String | 是   | 设备名称，用于后台显示                      |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "DEV_1234567890",
    "deviceSerial": "BOX_001",
    "deviceName": "会议室1号盒子",
    "status": "offline"
  }
}
```

**注意**：设备序列号保存到本地SharedPreferences，后续直接使用。

---

### 接口2：绑定用户

**请求URL**

```
POST http://服务器地址:8080/api/devices/bind
Content-Type: application/json
```

**请求参数**

```json
{
  "deviceSerial": "BOX_001",
  "userIds": "QianChen,FangYiMing",
  "userNames": "钱辰,方一鸣"
}
```

| 参数         | 类型   | 必填 | 说明                           |
| ------------ | ------ | ---- | ------------------------------ |
| deviceSerial | String | 是   | 设备序列号                     |
| userIds      | String | 是   | 企业微信用户ID，多个用逗号分隔 |
| userNames    | String | 是   | 用户姓名，多个用逗号分隔       |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bindingId": "BIND_1234567890",
    "deviceSerial": "BOX_001",
    "userIds": "QianChen,FangYiMing",
    "userNames": "钱辰,方一鸣"
  }
}
```

**重要**：这个接口决定了录音完成后推送给哪些用户！

---

### 接口3：录音上传

**请求URL**

```
POST http://服务器地址:8080/api/meetings/upload-audio
Content-Type: multipart/form-data
```

**请求参数**（FormData格式）

| 参数         | 类型   | 必填 | 说明                                  |
| ------------ | ------ | ---- | ------------------------------------- |
| meetingId    | String | 是   | 会议ID（格式：设备序列号\_时间戳）    |
| title        | String | 是   | 会议标题                              |
| date         | String | 是   | 会议日期（格式：2024-01-15 10:00:00） |
| deviceSerial | String | 是   | 设备序列号                            |
| duration     | String | 否   | 会议时长（分钟）                      |
| file         | File   | 是   | 录音文件                              |

**meetingId生成规则**

```java
String meetingId = deviceSerial + "_" + System.currentTimeMillis();
// 例如：BOX_001_1705296000000
```

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "meetingId": "BOX_001_1705296000000",
    "audioUrl": "/uploads/1705296000000_meeting.wav",
    "fileName": "1705296000000_meeting.wav"
  }
}
```

---

## 三、Android端代码示例（Kotlin）

### 1. 配置参数

```kotlin
object Config {
    const val SERVER_URL = "http://服务器地址:8080"

    // 设备信息（写死在代码里，或从服务端获取）
    const val DEVICE_SERIAL = "BOX_001"
    const val DEVICE_NAME = "会议室1号盒子"

    // 绑定的用户（由管理员配置）
    const val BIND_USER_IDS = "QianChen,FangYiMing"
    const val BIND_USER_NAMES = "钱辰,方一鸣"
}
```

### 2. 网络请求工具类

```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class DeviceApiClient(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // 1. 注册设备
    fun registerDevice(serial: String, name: String): Result<Device> {
        val json = """{"deviceSerial":"$serial","deviceName":"$name"}"""

        val request = Request.Builder()
            .url("$baseUrl/api/devices/register")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            parseResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. 绑定用户
    fun bindDevice(serial: String, userIds: String, userNames: String): Result<Unit> {
        val json = """{
            "deviceSerial": "$serial",
            "userIds": "$userIds",
            "userNames": "$userNames"
        }"""

        val request = Request.Builder()
            .url("$baseUrl/api/devices/bind")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("绑定失败: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 上传录音
    fun uploadAudio(
        deviceSerial: String,
        title: String,
        audioFile: File
    ): Result<UploadResult> {
        val meetingId = "${deviceSerial}_${System.currentTimeMillis()}"

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("meetingId", meetingId)
            .addFormDataPart("title", title)
            .addFormDataPart("date", getCurrentDateTime())
            .addFormDataPart("deviceSerial", deviceSerial)
            .addFormDataPart("duration", "60")
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/meetings/upload-audio")
            .post(multipartBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            parseUploadResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun String.toRequestBody(mediaType: MediaType?): RequestBody {
        return RequestBody.create(mediaType, this)
    }
}
```

### 3. 录音服务

```kotlin
class RecordingService {

    private val apiClient = DeviceApiClient(Config.SERVER_URL)

    // 初始化：注册并绑定（首次使用时调用一次）
    suspend fun initDevice() {
        // 1. 注册设备
        val registerResult = apiClient.registerDevice(
            serial = Config.DEVICE_SERIAL,
            name = Config.DEVICE_NAME
        )

        if (registerResult.isFailure) {
            Log.e("RecordingService", "设备注册失败")
            return
        }

        // 2. 绑定用户
        val bindResult = apiClient.bindDevice(
            serial = Config.DEVICE_SERIAL,
            userIds = Config.BIND_USER_IDS,
            userNames = Config.BIND_USER_NAMES
        )

        if (bindResult.isSuccess) {
            Log.d("RecordingService", "设备注册并绑定成功")
            // 保存状态到本地，表示已初始化
            saveInitialized(true)
        } else {
            Log.e("RecordingService", "设备绑定失败")
        }
    }

    // 上传录音
    suspend fun uploadRecording(audioFile: File, title: String): Result<UploadResult> {
        val result = apiClient.uploadAudio(
            deviceSerial = Config.DEVICE_SERIAL,
            title = title,
            audioFile = audioFile
        )

        if (result.isSuccess) {
            Log.d("RecordingService", "上传成功")
        } else {
            Log.e("RecordingService", "上传失败: ${result.exceptionOrNull()?.message}")
        }

        return result
    }

    private fun saveInitialized(initialized: Boolean) {
        // 保存到SharedPreferences
    }
}
```

### 4. 使用示例

```kotlin
class MainActivity : AppCompatActivity {

    private val recordingService = RecordingService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 首次启动时初始化设备
        if (!isDeviceInitialized()) {
            CoroutineScope(Dispatchers.IO).launch {
                recordingService.initDevice()
            }
        }
    }

    // 录音结束按钮点击
    fun onRecordingComplete() {
        val audioFile = File(getRecordingPath())
        val title = "会议录音_${getCurrentDateTime()}"

        CoroutineScope(Dispatchers.IO).launch {
            val result = recordingService.uploadRecording(audioFile, title)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    Toast.makeText(this, "上传成功，将自动推送给绑定的用户", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "上传失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
```

---

## 四、注意事项

### 1. 绑定用户配置

有两种方式：

- **方式1**：写死在APP里（适合固定用户场景）
- **方式2**：从服务端获取（适合需要灵活配置的场景）

### 2. 支持的音频格式

- MP3 (.mp3)
- WAV (.wav)
- M4A (.m4a)
- AMR (.amr)

### 3. 文件大小限制

最大 500MB

### 4. 初始化时机

设备首次开机时调用一次即可，后续不需要重复调用。

---

## 五、测试接口

用curl测试：

```bash
# 1. 注册设备
curl -X POST http://localhost:8080/api/devices/register \
  -H "Content-Type: application/json" \
  -d '{"deviceSerial":"BOX_TEST","deviceName":"测试设备"}'

# 2. 绑定用户
curl -X POST http://localhost:8080/api/devices/bind \
  -H "Content-Type: application/json" \
  -d '{"deviceSerial":"BOX_TEST","userIds":"QianChen","userNames":"钱辰"}'

# 3. 上传录音（需要准备音频文件）
curl -X POST http://localhost:8080/api/meetings/upload-audio \
  -F "meetingId=BOX_TEST_1705296000000" \
  -F "title=测试会议" \
  -F "date=2024-01-15 10:00:00" \
  -F "deviceSerial=BOX_TEST" \
  -F "file=@test.wav"
```

---

## 六、流程图

```
┌─────────────┐
│  盒子首次开机  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  1. 注册设备     │ POST /api/devices/register
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  2. 绑定用户     │ POST /api/devices/bind
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│    保存配置      │ SharedPreferences
└─────────────────┘

========================================

日常使用流程：

┌─────────────┐
│  录音完成     │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  3. 上传录音     │ POST /api/meetings/upload-audio
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  服务器自动处理   │
│  ├─ 保存文件     │
│  ├─ 语音转写    │
│  └─ 推送用户    │
└─────────────────┘
```
