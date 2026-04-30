# 企业微信会议录音转写系统 - 部署文档

## 一、环境要求

### 1.1 运行环境

| 项目 | 要求 |
|-----|------|
| JDK版本 | 1.8 或更高 |
| MySQL版本 | 8.0 |
| 内存 | 最低 2GB，推荐 4GB+ |
| 磁盘 | 最低 10GB（用于存储音频文件）|

### 1.2 服务器要求

- 操作系统：Linux (CentOS 7+/Ubuntu 18+) 或 Windows Server
- 网络：需要能够访问企业微信API (`qyapi.weixin.qq.com`)
- 端口：8080（后端服务）

---

## 二、项目打包

### 2.1 打包命令

```bash
cd /path/to/project/backend
mvn clean package -DskipTests
```

### 2.2 生成文件

打包成功后，在 `target/` 目录下生成：
```
meeting-transcribe-1.0.0.jar
```

---

## 三、数据库配置

### 3.1 创建数据库

连接MySQL服务器，执行以下SQL：

```sql
CREATE DATABASE meeting_recording DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3.2 建表语句

在 `meeting_recording` 数据库中执行以下建表语句：

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企业微信用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `avatar` VARCHAR(500) COMMENT '用户头像',
    `mobile` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `department` VARCHAR(200) COMMENT '部门',
    `position` VARCHAR(100) COMMENT '职位',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
);

-- 设备表
CREATE TABLE IF NOT EXISTS `device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '设备唯一标识',
    `device_serial` VARCHAR(100) NOT NULL UNIQUE COMMENT '设备序列号',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_key` VARCHAR(200) COMMENT '设备密钥',
    `status` VARCHAR(20) DEFAULT 'offline' COMMENT '设备状态：online/offline',
    `last_online_time` TIMESTAMP NULL COMMENT '最后在线时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_device_serial` (`device_serial`),
    INDEX `idx_device_id` (`device_id`)
);

-- 设备绑定关系表
CREATE TABLE IF NOT EXISTS `device_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    `binding_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '绑定关系ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `device_serial` VARCHAR(100) NOT NULL COMMENT '设备序列号',
    `user_id` VARCHAR(64) NULL COMMENT '企业微信用户ID',
    `user_ids` VARCHAR(500) NULL COMMENT '多个用户ID（逗号分隔）',
    `user_names` VARCHAR(1000) NULL COMMENT '多个用户名（逗号分隔）',
    `binding_type` VARCHAR(20) DEFAULT 'manual' COMMENT '绑定类型：manual/auto',
    `bind_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `last_update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_binding_device_serial` (`device_serial`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_user_id` (`user_id`)
);

-- 会议记录表
CREATE TABLE IF NOT EXISTS `meeting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会议ID',
    `meeting_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '会议唯一标识',
    `title` VARCHAR(200) NOT NULL COMMENT '会议标题',
    `meeting_date` TIMESTAMP NOT NULL COMMENT '会议日期',
    `duration` VARCHAR(50) NULL COMMENT '会议时长',
    `participants` INT DEFAULT 0 COMMENT '参与人数',
    `status` VARCHAR(20) DEFAULT 'processing' COMMENT '状态：processing/completed/failed',
    `summary` TEXT NULL COMMENT '会议摘要',
    `audio_url` VARCHAR(500) NULL COMMENT '录音文件URL',
    `audio_file_path` VARCHAR(500) NULL COMMENT '录音文件路径',
    `device_serial` VARCHAR(100) NULL COMMENT '设备序列号',
    `user_id` VARCHAR(64) NULL COMMENT '创建用户ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_device_serial` (`device_serial`)
);

-- 会议标签表
CREATE TABLE IF NOT EXISTS `meeting_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`)
);

-- 转写记录表
CREATE TABLE IF NOT EXISTS `transcript` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转写ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `speaker` VARCHAR(100) NULL COMMENT '说话人',
    `time` VARCHAR(20) NULL COMMENT '时间点',
    `text` TEXT NULL COMMENT '转写文本',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`)
);

-- 推送记录表
CREATE TABLE IF NOT EXISTS `push_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '推送ID',
    `meeting_id` VARCHAR(64) NOT NULL COMMENT '会议ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '接收用户ID',
    `push_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    `push_status` VARCHAR(20) DEFAULT 'success' COMMENT '推送状态：success/failed',
    `error_message` TEXT NULL COMMENT '错误信息',
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`),
    INDEX `idx_user_id` (`user_id`)
);
```

---

## 四、配置文件

### 4.1 配置文件位置

在 JAR 包同级目录创建 `application.yml` 配置文件

### 4.2 配置文件内容

```yaml
server:
  port: 8080

spring:
  application:
    name: meeting-transcribe

  datasource:
    url: jdbc:mysql://【数据库地址】:3306/meeting_recording?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: 【数据库用户名】
    password: 【数据库密码】
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

wechat:
  corp:
    id: "【企业ID，ww开头的字符串】"
    agent:
      id: 【应用AgentId，数字】
      secret: "【应用Secret】"
    token-url: https://qyapi.weixin.qq.com/cgi-bin/gettoken
    user-info-url: https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo
    user-detail-url: https://qyapi.weixin.qq.com/cgi-bin/user/get
    oauth-url: https://open.weixin.qq.com/connect/oauth2/authorize
    qr-connect-url: https://open.work.weixin.qq.com/wwopen/sso/qrConnect
    media-url: https://qyapi.weixin.qq.com/cgi-bin/media/upload
    send-message-url: https://qyapi.weixin.qq.com/cgi-bin/message/send

jwt:
  secret: 【自定义JWT密钥，建议32位以上随机字符串】
  expiration: 86400000

file:
  upload-path: /data/meeting-transcribe/uploads/

siliconflow:
  api-key: "【SiliconFlow API Key】"
```

### 4.3 配置项说明

| 配置项 | 说明 | 示例 |
|-------|------|------|
| 数据库地址 | MySQL服务器IP或域名 | 192.168.1.100 |
| 数据库用户名 | MySQL用户名 | root |
| 数据库密码 | MySQL密码 | xxxxxx |
| 企业ID | 企业微信企业ID | wwxxxxx |
| 应用AgentId | 企业微信应用AgentId | 1000001 |
| 应用Secret | 企业微信应用Secret | xxxxxx |
| 文件存储路径 | 录音文件存储目录（需创建） | /data/meeting-transcribe/uploads/ |

---

## 五、启动服务

### 5.1 创建存储目录

```bash
mkdir -p /data/meeting-transcribe/uploads
```

### 5.2 启动命令

```bash
# 基本启动
java -jar meeting-transcribe-1.0.0.jar

# 后台运行
nohup java -jar meeting-transcribe-1.0.0.jar > app.log 2>&1 &

# 指定配置文件启动
java -jar meeting-transcribe-1.0.0.jar --spring.config.location=./application.yml
```

### 5.3 验证启动

```bash
# 检查进程
ps -ef | grep meeting-transcribe

# 检查端口
netstat -tlnp | grep 8080

# 检查日志
tail -f app.log
```

启动成功日志：
```
Started MeetingTranscribeApplication in x.xxx seconds
会议录音转写系统启动成功！
```

---

## 六、企业微信配置

### 6.1 应用配置

登录企业微信管理后台 (https://work.weixin.qq.com)

1. 进入「应用管理」
2. 找到对应应用
3. 设置「企业可信IP」为服务器公网IP

### 6.2 网页授权配置

1. 进入「应用管理」→「网页」
2. 设置「可信域名」为前端部署的域名
3. 如需要扫码登录，设置「授权回调域」

### 6.3 通讯录权限

1. 进入「应用管理」
2. 在「API」权限中开通：
   - 通讯录同步
   - 查看企业通讯录

### 6.4 消息推送配置

在应用设置中配置「接收消息」：
- 启用「接收消息」
- 设置「URL」: `http://公网域名/api/wechat/callback`
- 设置「Token」: 与配置文件一致
- 设置「EncodingAESKey」: 与配置文件一致

---

## 七、前端部署

### 7.1 前端构建

```bash
cd frontend
npm install
npm run build
```

### 7.2 前端配置

构建前修改 `vue.config.js` 中的后端API地址：

```javascript
module.exports = {
  outputDir: 'dist',
  publicPath: '/',
  devServer: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://后端服务地址:8080',
        changeOrigin: true
      }
    }
  }
}
```

### 7.3 前端部署

将 `dist` 目录部署到 Nginx 或其他Web服务器

---

## 八、验证测试

### 8.1 接口测试

```bash
# 测试健康检查
curl http://localhost:8080/api/devices/list

# 测试设备绑定
curl -X POST http://localhost:8080/api/devices/bind \
  -H "Content-Type: application/json" \
  -d '{"deviceSerial":"TEST001","userIds":"test_user"}'
```

### 8.2 企业微信测试

1. 在企业微信中打开应用
2. 检查是否自动识别用户身份
3. 测试设备绑定功能
4. 测试消息推送功能

---

## 九、常见问题

### 9.1 启动失败

检查日志错误信息，常见问题：
- 数据库连接失败：检查数据库地址、用户名、密码
- 端口被占用：更换端口或杀掉占用进程

### 9.2 企业微信接口调用失败

- 检查网络是否能访问 `qyapi.weixin.qq.com`
- 检查企业ID、AgentId、Secret是否正确
- 检查IP是否在可信IP列表中

### 9.3 文件上传失败

- 检查存储目录是否存在
- 检查目录读写权限

---

## 十、联系支持

如有问题，请联系开发团队。
