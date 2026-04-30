# 会议录音转文字系统

基于 Spring Boot + Vue + MySQL 的会议录音转文字系统，集成企业微信登录和录音文件自动推送功能。

## 技术栈

### 后端
- Spring Boot 2.7.18
- Spring Data JPA
- Spring Security
- MySQL 8.0
- JWT Token 认证
- OkHttp (企业微信 API 调用)

### 前端
- Vue 3
- Vue Router
- Element Plus
- Axios
- SCSS

## 功能特性

### 1. 企业微信登录
- **企业微信内打开**：自动识别企业微信环境，实现免登录
- **外部浏览器打开**：显示企业微信扫码登录二维码
- **JWT Token 认证**：基于 Token 的无状态认证

### 2. 设备绑定管理
- 设备注册：支持第三方录音设备注册
- 手动绑定：用户手动绑定设备
- 自动绑定：设备通过密钥自动绑定（预留接口）
- 绑定管理：查询、修改、解绑

### 3. 会议录音管理
- 会议记录列表
- 会议详情查看
- 录音文件上传
- 转写内容展示

### 4. 录音文件自动推送
- 设备完成录音后自动推送
- 推送给绑定该设备的用户
- 推送记录追踪

## 项目结构

```
wechatPush/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/wechat/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── entity/            # 实体类
│   │   ├── repository/        # 数据访问层
│   │   ├── service/           # 服务层
│   │   └── util/              # 工具类
│   └── src/main/resources/
│       ├── application.yml    # 配置文件
│       └── schema.sql         # 数据库初始化脚本
│
└── frontend/                   # Vue 前端
    ├── src/
    │   ├── api/               # API 调用
    │   ├── layout/            # 布局组件
    │   ├── router/            # 路由配置
    │   ├── styles/            # 样式文件
    │   └── views/             # 页面组件
    └── package.json
```

## 快速开始

### 1. 数据库配置

```sql
# 创建数据库
CREATE DATABASE meeting_transcribe DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行初始化脚本
mysql -u root -p meeting_transcribe < backend/src/main/resources/schema.sql
```

### 2. 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_transcribe
    username: root
    password: your_password

wechat:
  corp:
    id: your_corp_id              # 企业微信 CorpID
    agent:
      id: your_agent_id            # 应用 AgentId
      secret: your_agent_secret    # 应用 Secret

jwt:
  secret: your_jwt_secret_key      # JWT 密钥
```

### 3. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 企业微信配置

### 1. 创建自建应用

1. 登录企业微信管理后台
2. 应用管理 → 自建 → 创建应用
3. 配置应用主页：`http://your-domain.com`
4. 配置可信域名：`your-domain.com`
5. 获取 AgentId 和 Secret

### 2. 配置 OAuth 回调

在企业微信应用设置中：
- 网页授权及JS-SDK：设置可信域名
- 企业微信授权登录：设置 Web 网页

### 3. 配置 IP 白名单

在企业微信管理后台：
- 管理工具 → 企业信息 → 企业ID
- 管理工具 → 通讯录同步 → 设置 IP 白名单

## API 接口

### 认证接口

```
GET  /api/auth/wechat/oauth-url     # 获取企业微信 OAuth URL
GET  /api/auth/wechat/qr-url        # 获取扫码登录二维码 URL
GET  /api/auth/wechat/callback      # OAuth 回调接口
GET  /api/auth/user/info            # 获取用户信息
POST /api/auth/logout               # 退出登录
```

### 设备接口

```
POST /api/devices/register          # 注册设备
POST /api/devices/bind              # 绑定设备
POST /api/devices/unbind            # 解绑设备
POST /api/devices/update-binding    # 更新绑定
GET  /api/devices/list              # 获取设备列表
GET  /api/devices/bindings          # 获取绑定列表
GET  /api/devices/my-binding        # 获取我的绑定
POST /api/devices/heartbeat         # 设备心跳
```

### 会议接口

```
GET  /api/meetings/list             # 获取会议列表
GET  /api/meetings/detail           # 获取会议详情
POST /api/meetings/create           # 创建会议
POST /api/meetings/upload-audio     # 上传录音
POST /api/meetings/update-status    # 更新状态
POST /api/meetings/save-transcripts # 保存转写记录
```

## 录音文件推送流程

1. **设备完成录音**：设备录音完成后，调用 `/api/meetings/create` 创建会议记录
2. **上传录音文件**：调用 `/api/meetings/upload-audio` 上传录音文件
3. **更新状态**：调用 `/api/meetings/update-status` 将状态更新为 `completed`
4. **自动推送**：系统自动查询设备绑定关系，推送录音文件给绑定用户

## 部署说明

### 生产环境配置

1. **修改前端 API 地址**：
```javascript
// frontend/src/api/index.js
baseURL: 'https://your-domain.com/api'
```

2. **配置 Nginx 反向代理**：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

3. **打包前端**：
```bash
cd frontend
npm run build
```

4. **打包后端**：
```bash
cd backend
mvn clean package
java -jar target/meeting-transcribe-1.0.0.jar
```

## 注意事项

1. **企业微信配置**：确保企业微信应用配置正确，包括可信域名、IP 白名单等
2. **数据库连接**：确保 MySQL 服务正常运行，数据库配置正确
3. **文件上传**：确保 `file.upload-path` 目录存在且有写入权限
4. **HTTPS**：生产环境建议使用 HTTPS
5. **Token 过期**：JWT Token 默认 24 小时过期，可在配置文件中调整

## 常见问题

### 1. 企业微信登录失败
- 检查企业微信配置是否正确
- 检查可信域名是否设置
- 检查 IP 白名单是否配置

### 2. 录音文件推送失败
- 检查设备是否已绑定用户
- 检查企业微信 access_token 是否有效
- 检查文件大小是否超过 20MB 限制

### 3. 跨域问题
- 后端已配置 CORS，允许所有域名访问
- 生产环境建议配置具体的允许域名

## 许可证

MIT License
